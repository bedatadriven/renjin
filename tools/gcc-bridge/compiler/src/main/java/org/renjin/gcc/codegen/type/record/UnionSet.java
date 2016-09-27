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

import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.Iterator;
import java.util.List;

/**
 * One or more {@link GimpleRecordTypeDef}s that need to have compatible memory layout.
 */
public class UnionSet {
  private final List<GimpleRecordTypeDef> unions;
  private final List<GimpleRecordTypeDef> records;
  
  private final FieldTypeSet typeSet;

  public UnionSet(List<GimpleRecordTypeDef> unions, List<GimpleRecordTypeDef> records) {
    this.unions = unions;
    this.records = records;
    this.typeSet = new FieldTypeSet(unions, records);
  }

  public List<GimpleRecordTypeDef> getUnions() {
    return unions;
  }

  public List<GimpleRecordTypeDef> getRecords() {
    return records;
  }
  
  public int getSize() {
    return records.size();
  }

  public boolean isSingleton() {
    return records.size() == 1 && unions.isEmpty();
  }

  public GimpleRecordTypeDef singleton() {
    return Iterables.getOnlyElement(records);
  }

  public FieldTypeSet getTypeSet() {
    return typeSet;
  }

  public Iterable<GimpleRecordTypeDef> getAllTypes() {
    return Iterables.concat(unions, records);
  }

  public String name() {
    String commonPrefix = common(true);
    String commonSuffix = common(false);
    
    String name;
    if(commonPrefix.length() >= commonSuffix.length()) {
      name = commonPrefix;
    } else {
      name = commonSuffix;
    }
    
    if(name.length() <= 1) {
      // If we don't have a nice common prefix/suffix
      // then use the name of the union instead.
      if(unions.size() == 1) {
        String unionName = Strings.nullToEmpty(unions.get(0).getName());
        if(unionName.length() > name.length()) {
          name = unionName;
        }
      }
    }
    
    if(Strings.isNullOrEmpty(name)) {
      return "record";
    } else {
      return name;
    }
  }

  private String common(boolean prefix) {
    String name = null;
    for (GimpleRecordTypeDef typeDef : getAllTypes()) {
      if(typeDef.getName() != null) {
        if(name == null) {
          name = typeDef.getName();
        } else if(prefix) {
          name = Strings.commonPrefix(name, typeDef.getName());
        } else {
          name = Strings.commonSuffix(name, typeDef.getName());
        }
      }
    }
    return Strings.nullToEmpty(name);
  }

  public int sizeOf() {
    Iterator<GimpleRecordTypeDef> it = getAllTypes().iterator();
    int size = it.next().getSize();
    while(it.hasNext()) {
      if(it.next().getSize() != size) {
        throw new IllegalStateException("inconsistent record sizes");
      }
    }
    return size / 8;
  }

  public String debugString() {
    StringBuilder sb = new StringBuilder();
    for (GimpleRecordTypeDef typeDef : getAllTypes()) {
      sb.append(typeDef).append("\n");
    }
    return sb.toString();
  }
}
