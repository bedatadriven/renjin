/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.vptr.VPtrRecordTypeStrategy;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleRecordType;
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
  private Map<String, ProvidedTypeStrategy> recordNameMap = new HashMap<>();

  /**
   * Map from external provided type to the strategy.
   */
  private Map<Type, ProvidedTypeStrategy> providedTypeMap = new HashMap<>();

  public void init(List<GimpleCompilationUnit> units, Map<String, Type> providedRecordTypes) {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleRecordTypeDef recordTypeDef : unit.getRecordTypes()) {
        typeDefMap.put(recordTypeDef.getId(), recordTypeDef);

        if (providedRecordTypes.containsKey(recordTypeDef.getName())) {
          Type jvmClass = providedRecordTypes.get(recordTypeDef.getName());
          ProvidedTypeStrategy strategy = new ProvidedTypeStrategy(recordTypeDef, jvmClass);

          recordNameMap.put(recordTypeDef.getName(), strategy);
          providedTypeMap.put(jvmClass, strategy);
        }
      }
    }
  }

  public GimpleRecordTypeDef getRecordTypeDef(GimpleRecordType type) {
    return getRecordTypeDef(type.getId());
  }

  public RecordTypeStrategy get(String recordTypeId) {

    GimpleRecordTypeDef def = getRecordTypeDef(recordTypeId);

    if(recordNameMap.containsKey(def.getName())) {
      return recordNameMap.get(def.getName());
    }

    return new VPtrRecordTypeStrategy(def);
  }

  private GimpleRecordTypeDef getRecordTypeDef(String recordTypeId) {
    GimpleRecordTypeDef def = typeDefMap.get(recordTypeId);
    if(def == null) {
      throw new IllegalStateException("Cannot find type def for " + recordTypeId);
    }
    return def;
  }

  public boolean isMappedToRecordType(Class<?> type) {
    return providedTypeMap.containsKey(Type.getType(type));
  }

  public PointerTypeStrategy getPointerStrategyFor(Class<?> type) {
    RecordTypeStrategy strategy = getStrategyFor(type);
    return strategy.pointerTo();
  }

  public RecordTypeStrategy getStrategyFor(Class<?> type) {
    RecordTypeStrategy strategy = providedTypeMap.get(Type.getType(type));
    Preconditions.checkNotNull(strategy, "No strategy for class " + type);
    return strategy;
  }
}
