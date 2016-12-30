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

import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.*;

/**
 * Builds a list of distinct GimpleRecordTypes across compilation units.
 * 
 * 
 */
public class RecordTypeDefCanonicalizer {

  /**
   * Map from GCC id to the GimpleRecordTypeDef
   */
  private Map<String, GimpleRecordTypeDef> idMap = Maps.newHashMap();

  /**
   * Map from GCC id to the canonical instance of the GimpleRecordTypeDef
   */
  private Map<String, GimpleRecordTypeDef> idToCanonicalMap = Maps.newHashMap();

  /**
   * Map from GCC id to declaration name
   */
  private Map<String, String> nameMap = Maps.newHashMap();
  
  private List<GimpleRecordTypeDef> canonical = Lists.newArrayList();
  
  private TreeLogger logger;
  
  public static Collection<GimpleRecordTypeDef> canonicalize(TreeLogger parentLogger, List<GimpleCompilationUnit> units) {
    TreeLogger logger = parentLogger.branch("Canonicalizing Record Type Defs");
    RecordTypeDefCanonicalizer transformer = new RecordTypeDefCanonicalizer(logger, units);
    transformer.updateAllTypes(units);
    
    return transformer.canonical;
  }
  
  public static Collection<GimpleRecordTypeDef> prune(List<GimpleCompilationUnit> units, 
                                                      Collection<GimpleRecordTypeDef> canonicalTypeDefs) {
    
    RecordTypeUseFinder finder = new RecordTypeUseFinder(canonicalTypeDefs);
    finder.visit(units);

    RecordFieldUseFinder fieldFinder = new RecordFieldUseFinder(canonicalTypeDefs);
    fieldFinder.visit(units);

    // Include only those that are actually used
    Set<GimpleRecordTypeDef> usedDefs = new HashSet<>();
    for (GimpleRecordTypeDef typeDef : canonicalTypeDefs) {
      if(finder.isUsed(typeDef)) {
        usedDefs.add(typeDef);
      }
    }
    return usedDefs;
  }
  
  private RecordTypeDefCanonicalizer(TreeLogger logger, List<GimpleCompilationUnit> units) {
    this.logger = logger;
    
    // Make a list of distinct record types, starting with the complete list 
    // of declared record types across all units, which will include duplicates
    List<GimpleRecordTypeDef> distinct = Lists.newArrayList();
    for (GimpleCompilationUnit unit : units) {
      distinct.addAll(unit.getRecordTypes());
    }

    boolean changing;
    do {

      System.out.println("*** ITERATION STARTING *** ");
      
      changing = false;

      // Remove duplicates using our key function
      Map<String, GimpleRecordTypeDef> keyMap = new HashMap<>();
      for (GimpleRecordTypeDef recordTypeDef : distinct) {
        
        if(!Strings.isNullOrEmpty(recordTypeDef.getName())) {
          nameMap.put(recordTypeDef.getId(), recordTypeDef.getName());
        }
        
        String key = key(recordTypeDef);
        
        System.out.println(String.format("%s %s => %s", recordTypeDef.getId(), recordTypeDef.getName(), key));
        
        GimpleRecordTypeDef canonical = keyMap.get(key);
        if (canonical == null) {
          // first time seen, this is a canonical record
          keyMap.put(key, recordTypeDef);
        } else {
          // duplicate of already seen structure, map its id to the canonical version
          mergeInto(canonical, recordTypeDef);
          idToCanonicalMap.put(recordTypeDef.getId(), canonical);
          
          // remap any structures pointing to this one
          remapFrom(recordTypeDef.getId(), canonical);
          
          changing = true;
        }
      }

      // update our list of distinct types
      distinct = Lists.newArrayList(keyMap.values());

      // among the distinct record types, update _their_ fields to the canonical 
      // field record types, and see if this yields further duplicates.
      for (GimpleRecordTypeDef recordTypeDef : distinct) {
        CANONIZING_VISITOR.visit(recordTypeDef);
      }
    } while(changing);
    
    this.canonical = distinct;
  }


  /**
   * We do some type erasure of record pointers so it's important to merge that information
   * into the canonical record def to avoid loosing it.
   *
   */
  private void mergeInto(GimpleRecordTypeDef canonical, GimpleRecordTypeDef recordTypeDef) {
    Map<Integer, GimpleField> fieldMap = new HashMap<>();
    for (GimpleField field : canonical.getFields()) {
      fieldMap.put(field.getOffset(), field);
    }

    for (GimpleField field : recordTypeDef.getFields()) {
      GimpleField canonicalField = fieldMap.get(field.getOffset());
      if(canonicalField != null && !canonicalField.getType().equals(field.getType())) {
        canonical.getFields().add(field);
      }
    }
  }

  private void updateAllTypes(List<GimpleCompilationUnit> units) {


    for (GimpleCompilationUnit unit : units) {
      for (int i = 0; i < unit.getRecordTypes().size(); i++) {
        GimpleRecordTypeDef recordTypeDef = unit.getRecordTypes().get(i);
        GimpleRecordTypeDef canonicalDef = idToCanonicalMap.get(recordTypeDef.getId());
        if (canonicalDef != null) {
          unit.getRecordTypes().set(i, canonicalDef);
        }
      }
    }

    CANONIZING_VISITOR.visit(units);
  }

  private void remapFrom(String oldCanonicalId, GimpleRecordTypeDef canonical) {
    Set<String> toRemap = Sets.newHashSet();
    for (Map.Entry<String, GimpleRecordTypeDef> entry : idToCanonicalMap.entrySet()) {
      if(entry.getValue().getId().equals(oldCanonicalId)) {
        toRemap.add(entry.getKey());
      }
    }
    for (String id : toRemap) {
      idToCanonicalMap.put(id, canonical);
    }
  }

  private String key(GimpleRecordTypeDef typeDef) {
    StringBuilder key = new StringBuilder();
    if(typeDef.getName() != null) {
      key.append(typeDef.getName());
    }
    appendKeyTo(typeDef, key);
    return key.toString();
  }
  
  public void appendKeyTo(GimpleRecordTypeDef def, StringBuilder key) {
    key.append("{");
    boolean needsComma = false;
    for (GimpleField gimpleField : def.getFields()) {
      if(needsComma) {
        key.append(",");
      }
      key.append(gimpleField.getName()).append(":");
      appendTypeKeyTo(def, gimpleField.getType(), key);
      needsComma = true;
    }
    key.append("}");
  }
  
  private void appendTypeKeyTo(GimpleRecordTypeDef rootRecordTypeDef, GimpleType type, StringBuilder key) {
    if(type instanceof GimpleRecordType) {
      GimpleRecordType recordType = (GimpleRecordType) type;
      if(recordType.getId().equals(rootRecordTypeDef.getId())) {
        key.append("recursive");
      } else {
        key.append("record(").append(recordType.getId()).append(")");
      }
    
    } else if(type instanceof GimpleIndirectType) {
      if(type.getBaseType() instanceof GimpleRecordType) {
        key.append("*record");
      } else {
        key.append("*");
        appendTypeKeyTo(rootRecordTypeDef, type.getBaseType(), key);
      }
    
    } else if(type instanceof GimpleArrayType) {
      key.append("[");
      appendTypeKeyTo(rootRecordTypeDef, ((GimpleArrayType) type).getComponentType(), key);
      
    } else if(type instanceof GimpleComplexType) {
      key.append("complex");
    } else if(type instanceof GimpleRealType) {
      key.append("real").append(((GimpleRealType) type).getPrecision());
    } else if(type instanceof GimpleIntegerType) {
      key.append("int").append(((GimpleIntegerType) type).getPrecision());
 
    } else if(type instanceof GimpleFunctionType) {
      key.append("fun");
    
    } else if(type instanceof GimpleBooleanType) {
      key.append("bool");
    
    } else if(type instanceof GimpleVoidType) {
      key.append("void");
    }
  }
  
  private RecordTypeUseVisitor CANONIZING_VISITOR = new RecordTypeUseVisitor() {

    @Override
    protected void visitRecordType(GimpleRecordType recordType) {
      GimpleRecordTypeDef canonicalDef = idToCanonicalMap.get(recordType.getId());
      if(canonicalDef != null) {
        recordType.setId(canonicalDef.getId());
      }

      // Populate name field to help with debugging
      String name = nameMap.get(recordType.getId());
      if(name != null) {
        recordType.setName(name);
      }
    }
  };
  
}
