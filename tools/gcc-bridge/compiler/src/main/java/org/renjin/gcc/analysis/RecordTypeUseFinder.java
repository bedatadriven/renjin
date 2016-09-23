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
package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enumerate Record types actually used by compiled code
 */
public class RecordTypeUseFinder extends RecordTypeUseVisitor {
  
  private final Map<String, GimpleRecordTypeDef> typeDefMap = new HashMap<>();
  
  private final Set<String> usedTypeDefs = Sets.newHashSet();

  public RecordTypeUseFinder(Iterable<GimpleRecordTypeDef> canonicalTypeDefs) {
    for (GimpleRecordTypeDef canonicalTypeDef : canonicalTypeDefs) {
      typeDefMap.put(canonicalTypeDef.getId(), canonicalTypeDef);
    }
  }

  @Override
  protected void visitRecordType(GimpleRecordType type) {
    GimpleRecordTypeDef typeDef = typeDefMap.get(type.getId());
    
    if(typeDef == null) {
      throw new IllegalStateException();
    }
    
    if(usedTypeDefs.add(typeDef.getId())) {
      visit(typeDef);
    }
  }

  public boolean isUsed(GimpleRecordTypeDef typeDef) {
    return usedTypeDefs.contains(typeDef.getId());
  }
}
