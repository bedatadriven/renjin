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

import org.renjin.gcc.analysis.RecordUsageAnalyzer;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.vptr.VPtrRecordTypeStrategy;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordTypeDefMap {

  private Map<String, GimpleRecordTypeDef> typeDefMap = new HashMap<>();

  /**
   * Map from record *names* to the provided class strategy.
   */
  private Map<String, RecordClassTypeStrategy> recordNameMap = new HashMap<>();

  /**
   * Map from external provided type to the strategy.
   */
  private Map<Type, RecordClassTypeStrategy> providedTypeMap = new HashMap<>();

  public void init(TypeOracle typeOracle, List<GimpleCompilationUnit> units, Map<String, Class> providedRecordTypes) {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleRecordTypeDef recordTypeDef : unit.getRecordTypes()) {
        typeDefMap.put(recordTypeDef.getId(), recordTypeDef);

        if (providedRecordTypes.containsKey(recordTypeDef.getName())) {
          Class jvmClass = providedRecordTypes.get(recordTypeDef.getName());
          ProvidedLayout layout = new ProvidedLayout(recordTypeDef, Type.getType(jvmClass));
          RecordClassTypeStrategy strategy = new RecordClassTypeStrategy(recordTypeDef, layout);
          strategy.setUnitPointer(true);

          recordNameMap.put(recordTypeDef.getName(), strategy);
          providedTypeMap.put(Type.getType(jvmClass), strategy);
        }
      }
    }

    for (RecordClassTypeStrategy recordClassTypeStrategy : recordNameMap.values()) {
      recordClassTypeStrategy.getLayout().linkFields(typeOracle);
    }

    RecordUsageAnalyzer recordUsage = new RecordUsageAnalyzer(typeDefMap.values());
    recordUsage.analyze(units);

    for (GimpleRecordTypeDef recordTypeDef : typeDefMap.values()) {
      RecordClassTypeStrategy provided = recordNameMap.get(recordTypeDef.getName());
      if(provided != null) {
        if(!recordUsage.unitPointerAssumptionsHoldFor(recordTypeDef)) {
          provided.setUnitPointer(false);
        }
      }
    }

  }


  public RecordTypeStrategy get(String recordTypeId) {

    GimpleRecordTypeDef def = typeDefMap.get(recordTypeId);
    if(def == null) {
      throw new IllegalStateException("Cannot find type def for " + recordTypeId);
    }

    if(recordNameMap.containsKey(def.getName())) {
      return recordNameMap.get(def.getName());
    }

    return new VPtrRecordTypeStrategy(def);
  }

  public boolean isMappedToRecordType(Class<?> type) {
    return providedTypeMap.containsKey(Type.getType(type));
  }

  public RecordUnitPtrStrategy getPointerStrategyFor(Class<?> type) {
    RecordClassTypeStrategy strategy = getStrategyFor(type);
    return strategy.pointerToUnit();
  }

  public RecordClassTypeStrategy getStrategyFor(Class<?> type) {
    RecordClassTypeStrategy strategy = providedTypeMap.get(Type.getType(type));
    Preconditions.checkNotNull(strategy, "No strategy for class " + type);
    return strategy;
  }


}
