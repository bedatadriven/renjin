package org.renjin.gcc.analysis;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.*;
import org.renjin.gcc.gimple.type.*;

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
  
  
  public static Collection<GimpleRecordTypeDef> canonicalize(List<GimpleCompilationUnit> units) {
    RecordTypeDefCanonicalizer transformer = new RecordTypeDefCanonicalizer(units);
    
    transformer.updateAllTypes(units);
    
    return transformer.canonical;
  }
  
  private RecordTypeDefCanonicalizer(List<GimpleCompilationUnit> units) {
    
    // Make a list of distinct record types, starting with the complete list 
    // of declared record types across all units, which will include duplicates
    List<GimpleRecordTypeDef> distinct = Lists.newArrayList();
    for (GimpleCompilationUnit unit : units) {
      distinct.addAll(unit.getRecordTypes());
    }

    boolean changing;
    do {

      changing = false;

      // Remove duplicates using our key function
      Map<String, GimpleRecordTypeDef> keyMap = new HashMap<>();
      for (GimpleRecordTypeDef recordTypeDef : distinct) {
        
        if(!Strings.isNullOrEmpty(recordTypeDef.getName())) {
          nameMap.put(recordTypeDef.getId(), recordTypeDef.getName());
        }
        
        String key = key(recordTypeDef);
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
        updateFieldTypes(recordTypeDef);
      }
    } while(changing);
    
    this.canonical = distinct;
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
      key.append("*");
      appendTypeKeyTo(rootRecordTypeDef, type.getBaseType(), key);
    
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

  private void updateAllTypes(List<GimpleCompilationUnit> units) {

    for (Map.Entry<String, GimpleRecordTypeDef> entry : idToCanonicalMap.entrySet()) {
      System.out.println("Mapping " + entry.getKey() + " -> "  + entry.getValue().getId());
    }
    
    // Ensure that canonical type defs reference other canonical type defs in their fields
    for (GimpleRecordTypeDef recordTypeDef : idToCanonicalMap.values()) {
      updateFieldTypes(recordTypeDef);
    }
    
    // Replace ids in each compilation unit with references to the canonical type
    for (GimpleCompilationUnit unit : units) {

      for (int i = 0; i < unit.getRecordTypes().size(); i++) {
        GimpleRecordTypeDef recordTypeDef = unit.getRecordTypes().get(i);
        GimpleRecordTypeDef canonicalDef = idToCanonicalMap.get(recordTypeDef.getId());
        if (canonicalDef != null) {
          unit.getRecordTypes().set(i, canonicalDef);
        }
      }

      for (GimpleVarDecl decl : unit.getGlobalVariables()) {
        updateType(decl.getType());
        if(decl.getValue() != null) {
          updateTypes(decl.getValue());
        }
      }
      
      for (GimpleFunction function : unit.getFunctions()) {
        updateType(function.getReturnType());
        for (GimpleParameter gimpleParameter : function.getParameters()) {
          updateType(gimpleParameter.getType());
        }
        for (GimpleVarDecl decl : function.getVariableDeclarations()) {
          updateType(decl.getType());
        }
        for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
          for (GimpleStatement statement : basicBlock.getStatements()) {
            updateTypes(statement);
          }
        }
      }
    }
  }

  private void updateFieldTypes(GimpleRecordTypeDef recordTypeDef) {
    for (GimpleField gimpleField : recordTypeDef.getFields()) {
      updateType(gimpleField.getType());
    }
  }


  private void updateTypes(Iterable<GimpleType> types) {
    for (GimpleType type : types) {
      updateType(type);
    }
  }
  
  private void updateType(GimpleType type) {
    if(type instanceof GimpleRecordType) {
      GimpleRecordType recordType = (GimpleRecordType) type;
      GimpleRecordTypeDef canonicalDef = idToCanonicalMap.get(recordType.getId());
      if(canonicalDef != null) {
        recordType.setId(canonicalDef.getId());
      }

      // Populate name field to help with debugging
      String name = nameMap.get(recordType.getId());
      if(name != null) {
        recordType.setName(name);
      }
      
    } else if(type instanceof GimpleIndirectType) {
      updateType(type.getBaseType());  
    } else if(type instanceof GimpleArrayType) {
      updateType(((GimpleArrayType) type).getComponentType());
    } else if(type instanceof GimpleFunctionType) {
      GimpleFunctionType functionType = (GimpleFunctionType) type;
      updateType(functionType.getReturnType());
      updateTypes(functionType.getArgumentTypes());
    }
  }


  private void updateTypes(GimpleStatement gimpleIns) {
    if(gimpleIns instanceof GimpleReturn) {
      updateTypes(((GimpleReturn) gimpleIns).getValue());
    } else if(gimpleIns instanceof GimpleAssignment) {
      GimpleAssignment assignment = (GimpleAssignment) gimpleIns;
      updateTypes(assignment.getLHS());
      updateTypes(assignment.getOperands());
    } else if(gimpleIns instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) gimpleIns;
      updateTypes(call.getLhs());
      updateTypes(call.getFunction());
      updateTypes(call.getOperands());
    } else if(gimpleIns instanceof GimpleConditional) {
      GimpleConditional conditional = (GimpleConditional) gimpleIns;
      updateTypes(conditional.getOperands());
    } else if(gimpleIns instanceof GimpleSwitch) {
      GimpleSwitch gimpleSwitch = (GimpleSwitch) gimpleIns;
      updateTypes(gimpleSwitch.getValue());
    }
  }

  private void updateTypes(List<GimpleExpr> operands) {
    for (GimpleExpr operand : operands) {
      updateTypes(operand);
    }
  }

  private void updateTypes(GimpleExpr expr) {
    if(expr != null) {
      updateType(expr.getType());
      if (expr instanceof GimpleAddressOf) {
        updateTypes(((GimpleAddressOf) expr).getValue());
      } else if (expr instanceof GimpleMemRef) {
        updateTypes(((GimpleMemRef) expr).getPointer());
      } else if (expr instanceof GimpleComponentRef) {
        GimpleComponentRef ref = (GimpleComponentRef) expr;
        updateTypes(ref.getValue());
        updateTypes(ref.getMember());
      } else if (expr instanceof GimpleArrayRef) {
        updateTypes(((GimpleArrayRef) expr).getArray());
      } else if (expr instanceof GimpleNopExpr) {
        updateTypes(((GimpleNopExpr) expr).getValue());
      } else if (expr instanceof GimpleConstructor) {
        GimpleConstructor constructor = (GimpleConstructor) expr;
        for (GimpleConstructor.Element element : constructor.getElements()) {
          updateTypes(element.getValue());
        }
      }
    }
  }
}
