package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.analysis.RecordUsageAnalyzer;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;

import java.io.File;
import java.io.IOException;
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

  public void build() {

    // The first thing we need to do is identify UnionSets, which tell us
    // which record types need to have a compatible layout in memory.
    
    UnionSetBuilder unionSetBuilder = new UnionSetBuilder(recordTypeDefs);
    List<UnionSet> sets = unionSetBuilder.build();

    for (UnionSet set : sets) {
      if(set.isSingleton()) {
        // Simple case, we can do as we like
        typeOracle.addRecordType(set.singleton(), strategyForSingleton(set));
      
      } else if(set.getTypeSet().isEmpty()) {
        // Union of several empty types can all be represented as 
        // java.lang.Object
        for (GimpleRecordTypeDef typeDef : set.getAllTypes()) {
          typeOracle.addRecordType(typeDef, emptyRecordStrategy(typeDef));
        }

      } else {
        // Try to see if we can represent all values in the type 
        Optional<Type> commonType = set.getTypeSet().tryComputeCommonType();
        if(commonType.isPresent()) {
          for (GimpleRecordTypeDef typeDef : set.getAllTypes()) {
            typeOracle.addRecordType(typeDef, new RecordArrayTypeStrategy(typeDef, commonType.get()));
          }
        } else {
          throw new UnsupportedOperationException("TODO");

        }
      }
    }

    // Now that the record types are all registered, we can link the fields to their
    // FieldGenerators
    for (RecordTypeStrategy recordTypeStrategy : typeOracle.getRecordTypes()) {
      try {
        recordTypeStrategy.linkFields(typeOracle);
      } catch (Exception e) {
        throw new InternalCompilerException(String.format("Exception linking record %s: %s",
            recordTypeStrategy.getRecordTypeDef().getName(),
            e.getMessage()), e);
      }
    }
  }

  private RecordTypeStrategy strategyForSingleton(UnionSet set) {
    if(isProvided(set.singleton())) {
      return providedTypeStrategy(set.singleton());
    }
    
    if(set.getTypeSet().isEmpty()) {
      return emptyRecordStrategy(set.singleton());
    } 
    
    if(set.getTypeSet().isBestRepresentableAsArray()) {
      return new RecordArrayTypeStrategy(set.singleton(), set.getTypeSet().uniqueValueType());
    }
    
    // Otherwise, we need to build a JVM class for this record
    return classStrategy(set.singleton());
  }


  public void writeClasses(File outputDirectory) throws IOException {
    // Finally write out the record class files for those records which are  not provided
    for (RecordTypeStrategy recordTypeStrategy : typeOracle.getRecordTypes()) {
      recordTypeStrategy.writeClassFiles(outputDirectory);
    }
  }

  /**
   * Builds a strategy for a record type that is to be mapped to an existing JVM class.
   * 
   */
  private RecordClassTypeStrategy providedTypeStrategy(GimpleRecordTypeDef recordTypeDef) {
    Type providedType = Type.getType(providedRecordTypes.get(recordTypeDef.getName()));

    RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(recordTypeDef);
    strategy.setProvided(true);
    strategy.setJvmType(providedType);

    strategy.setUnitPointer(usage.unitPointerAssumptionsHoldFor(recordTypeDef));
    return strategy;
  }

  /**
   * Builds a strategy for a record with no fields. 
   * 
   * <p>These types must have some representation because they do occupy memory and should
   * have a consistent address, but to give us the maximum flexiblity to cast between 
   * nominally different types of empty records, we will represent values of these types
   * as instances of java.lang.Object</p>
   */
  private RecordTypeStrategy emptyRecordStrategy(GimpleRecordTypeDef recordTypeDef) {
    RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(recordTypeDef);
    strategy.setUnitPointer(usage.unitPointerAssumptionsHoldFor(recordTypeDef));
    strategy.setJvmType(Type.getType(Object.class));
    strategy.setProvided(true);

    return strategy;
  }


  private RecordClassTypeStrategy classStrategy(GimpleRecordTypeDef recordTypeDef) {
    RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(recordTypeDef);
    strategy.setUnitPointer(usage.unitPointerAssumptionsHoldFor(recordTypeDef));
    String recordClassName;
    if (recordTypeDef.getName() != null) {
      recordClassName =  String.format("%s$%s", recordClassPrefix, recordTypeDef.getName() + (nextRecordIndex++));
    } else {
      recordClassName = String.format("%s$Record%d", recordClassPrefix, nextRecordIndex++);
    }
    strategy.setJvmType(Type.getType("L" + recordClassName + ";"));
    return strategy;
  }
  
}
