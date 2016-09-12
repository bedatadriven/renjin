package org.renjin.gcc.analysis;

import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
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
          // duplicate of already seen structure, map it's id to the canonical version
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
