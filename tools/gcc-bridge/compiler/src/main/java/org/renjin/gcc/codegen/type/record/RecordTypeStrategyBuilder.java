package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.analysis.RecordUsageAnalyzer;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Puts our thinking cap on and figures out the best way to 
 * implement our record types.
 */
public class RecordTypeStrategyBuilder {


  private TypeOracle typeOracle;
  private final Collection<GimpleRecordTypeDef> recordTypeDefs;
  private final Map<String, Class> providedRecordTypes;
  private final RecordUsageAnalyzer usage;
  private String recordClassPrefix;
  
  private int nextRecordIndex;
  
  private List<RecordLayout> layouts = new ArrayList<>();

  public RecordTypeStrategyBuilder(
      TypeOracle typeOracle,
      Collection<GimpleRecordTypeDef> recordTypeDefs,
      Map<String, Class> providedRecordTypes,
      List<GimpleCompilationUnit> units) {
    this.typeOracle = typeOracle;

    this.recordTypeDefs = recordTypeDefs;
    this.providedRecordTypes = providedRecordTypes;

    // Analyze record type usage to determine strategy for generate code involving records
    // (must be done after void ptr inference)
    this.usage = new RecordUsageAnalyzer(recordTypeDefs);
    this.usage.analyze(units);
  }

  public void setRecordClassPrefix(String recordClassPrefix) {
    this.recordClassPrefix = recordClassPrefix;
  }
  
  private boolean isProvided(GimpleRecordTypeDef recordTypeDef) {
    if(recordTypeDef.getName() == null) {
      return false;
    } else {
      return providedRecordTypes.containsKey(recordTypeDef.getName());
    }
  }

  public void build(TreeLogger parentLogger) {

    TreeLogger logger = parentLogger.branch("Building RecordTypeStrategies...");
    
    // The first thing we need to do is identify UnionSets, which tell us
    // which record types need to have a compatible layout in memory.
    
    UnionSetBuilder unionSetBuilder = new UnionSetBuilder(recordTypeDefs);
    List<UnionSet> sets = unionSetBuilder.build();

    for (UnionSet set : sets) {
      
      TreeLogger setLogger = logger.branch(TreeLogger.Level.DEBUG, "Union set " + set.name());
      setLogger.debug("Set:", set.debugString());
      
      try {
        if (set.isSingleton()) {
          // Simple case, we can do as we like
          buildSingleton(logger, set);

        } else {
          buildUnion(setLogger, set);
        }
      } catch (Exception e) {
        dumpSet(set, e);
      }
    }
    
    // Now that the record types are all registered, we can link the fields to their
    // FieldGenerators
    for (RecordLayout layout : layouts) {
      layout.linkFields(typeOracle);
    }
  }

  private void dumpSet(UnionSet set, Exception e) {
    StringBuilder s = new StringBuilder();
    for (GimpleRecordTypeDef typeDef : set.getAllTypes()) {
      s.append(typeDef).append("\n");
    }
    
    throw new InternalCompilerException("Exception building union set '" + set.name() + 
        ": " + e.getMessage() + 
        s.toString(), e);
    
  }

  /**
   * Builds a strategy for a record with no fields. 
   *
   * <p>These types must have some representation because they do occupy memory and should
   * have a consistent address, but to give us the maximum flexiblity to cast between 
   * nominally different types of empty records, we will represent values of these types
   * as instances of java.lang.Object</p>
   */
  private void buildEmpty(UnionSet set) {

    boolean unitPointer = isUnitPointer(set);
    for (GimpleRecordTypeDef typeDef : set.getAllTypes()) {
      RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(typeOracle, typeDef, new EmptyRecordLayout());
      strategy.setUnitPointer(unitPointer);
      typeOracle.addRecordType(typeDef, strategy);
    }
  }

  /**
   * Builds a strategy for a "normal" record type that is not unioned with
   * any other record types.
   */
  private void buildSingleton(TreeLogger logger, UnionSet set) {
    
    if(isProvided(set.singleton())) {
      typeOracle.addRecordType(set.singleton(), providedTypeStrategy(set.singleton()));

    } else if(set.getTypeSet().isEmpty()) {
      buildEmpty(set);
      
    } else if(set.getTypeSet().isBestRepresentableAsArray()) {
      typeOracle.addRecordType(set.singleton(), 
          new RecordArrayTypeStrategy(set.singleton(), set.getTypeSet().uniquePrimitiveType()));

    } else {
      // Otherwise, we need to build a JVM class for this record
      buildClassStrategy(logger, set);
    }
  }
  
  private void buildUnion(TreeLogger logger, UnionSet set) {
    
    if(set.getTypeSet().isEmpty()) {
      logger.debug("Using EmptyRecordStrategy.");
      buildEmpty(set);
    
    } else {

      // Try to see if we can represent all values in the type 
      Optional<Type> commonType = set.getTypeSet().tryComputeCommonType();
      if (commonType.isPresent()) {
        logger.debug("Using RecordArrayTypeStrategy: " + commonType.get());

        for (GimpleRecordTypeDef typeDef : set.getAllTypes()) {
          typeOracle.addRecordType(typeDef, new RecordArrayTypeStrategy(typeDef, commonType.get()));
        }
      } else {
        // Fields are heterogeneous, 
        // we need to construct a class for this union
        buildClassStrategy(logger, set);
      }
    }
  }

  private void buildClassStrategy(TreeLogger logger, UnionSet set) {
    
    RecordClassLayout layout = new RecordClassLayout(set, nextRecordName(set.name()));
    logger.debug("Using RecordClassTypeStrategy: " + layout.getType());

    boolean unitPointer = false;
    logger.debug("unitPointer = " + unitPointer);

    for (GimpleRecordTypeDef typeDef : set.getAllTypes()) {
      RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(typeOracle, typeDef, layout);
      strategy.setUnitPointer(unitPointer);
      typeOracle.addRecordType(typeDef, strategy);
    }
    
    layouts.add(layout);
  }

  private boolean isUnitPointer(UnionSet set) {
    for (GimpleRecordTypeDef typeDef : set.getAllTypes()) {
      if(!usage.unitPointerAssumptionsHoldFor(typeDef)) {
        return false;
      }
    }
    return true;
  }


  public void writeClasses(File outputDirectory) throws IOException {
    
    // Make sure we write superclasses first
    
    
    // Finally write out the record class files for those records which are  not provided
    for (RecordLayout layout : layouts) {
      layout.writeClassFiles(outputDirectory);
    }
  }

  /**
   * Builds a strategy for a record type that is to be mapped to an existing JVM class.
   * 
   */
  private RecordClassTypeStrategy providedTypeStrategy(GimpleRecordTypeDef recordTypeDef) {
    Type providedType = Type.getType(providedRecordTypes.get(recordTypeDef.getName()));
    ProvidedLayout providedLayout = new ProvidedLayout(recordTypeDef, providedType);

    layouts.add(providedLayout);
    
    RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(typeOracle, recordTypeDef, providedLayout);
    strategy.setUnitPointer(usage.unitPointerAssumptionsHoldFor(recordTypeDef));
    return strategy;
  }


  private Type nextRecordName(String name) {
    
    String recordClassName = String.format("%s$%s", recordClassPrefix, name + (nextRecordIndex++));

    return Type.getType("L" + recordClassName + ";");
  }
  
}
