/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.analysis.RecordUsageAnalyzer;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
  
  private final Set<String> recordNames = new HashSet<>();

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

  public void build(TreeLogger logger) {

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
          buildSingleton(set);

        } else {
          buildUnion(set);
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

    // Write details to logging if enabled
    if(logger.isEnabled()) {
      for (GimpleRecordTypeDef recordTypeDef : recordTypeDefs) {
        String name = recordTypeDef.getName();
        if(name == null) {
          name = "anonymous_" + recordTypeDef.getId();
        }
        logger.dump("records", name, "def", recordTypeDef);
      }
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
  private void buildSingleton(UnionSet set) {
    
    if(isProvided(set.singleton())) {
      typeOracle.addRecordType(set.singleton(), providedTypeStrategy(set.singleton()));

    } else if(set.getTypeSet().isEmpty()) {
      buildEmpty(set);
      
    } else if(set.getTypeSet().isBestRepresentableAsArray()) {
      typeOracle.addRecordType(set.singleton(),
          new RecordArrayTypeStrategy(set.singleton(), set.getTypeSet().uniquePrimitiveType()));

    } else {
      // Otherwise, we need to build a JVM class for this record
      buildClassStrategy(set);
    }
  }
  
  private void buildUnion(UnionSet set) {
    
    if(set.getTypeSet().isEmpty()) {
      buildEmpty(set);
    
    } else {

      // Try to see if we can represent all values in the type 
      Optional<Type> commonType = set.getTypeSet().tryComputeCommonType();
      if (commonType.isPresent()) {
        for (GimpleRecordTypeDef typeDef : set.getAllTypes()) {
          typeOracle.addRecordType(typeDef, new RecordArrayTypeStrategy(typeDef, commonType.get()));
        }
      } else {
        // Fields are heterogeneous, 
        // we need to construct a class for this union
        buildClassStrategy(set);
      }
    }
  }

  private void buildClassStrategy(UnionSet set) {
    
    RecordClassLayout layout = new RecordClassLayout(set, nextRecordName(set.name()));
    boolean unitPointer = isUnitPointer(set);

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
    
    if(providedType.getInternalName().equals("org/renjin/sexp/SEXP")) {
      // TODO(alex): remove hard-coded rule
      strategy.setUnitPointer(true);
    } else {
      strategy.setUnitPointer(usage.unitPointerAssumptionsHoldFor(recordTypeDef));
    }
    return strategy;
  }


  private Type nextRecordName(String name) {
    
    String recordClassName = recordClassPrefix + uniqueName(name);

    return Type.getType("L" + recordClassName + ";");
  }

  private String uniqueName(String name) {
    if(!recordNames.contains(name)) {
      recordNames.add(name);
      return name;
    }
    int index = 0;
    String disambiguatedName;
    do {
      index++;
      disambiguatedName = name + index;
    } while(recordNames.contains(disambiguatedName));
    
    recordNames.add(disambiguatedName);
    return disambiguatedName;
  }

}
