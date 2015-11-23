package org.renjin.gcc.analysis;

import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.ins.*;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.gimple.type.GimpleComplexType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Builds a list of distinct GimpleRecordTypes across compilation units
 */
public class RecordTypeDefCanonicalizer {

  /**
   * Map from GCC id to the GimpleRecordTypeDef
   */
  private Map<String, GimpleRecordTypeDef> idMap = Maps.newHashMap();

  /**
   * Map from record type key to the canonical instance of the GimpleRecordTypeDef
   */
  private Map<String, GimpleRecordTypeDef> keyMap = Maps.newHashMap();

  /**
   * Map from GCC id to the canonical instance of the GimpleRecordTypeDef
   */
  private Map<String, GimpleRecordTypeDef> idToCanonicalMap = Maps.newHashMap();
  
  
  public static Collection<GimpleRecordTypeDef> canonicalize(List<GimpleCompilationUnit> units) {
    RecordTypeDefCanonicalizer transformer = new RecordTypeDefCanonicalizer(units);
    transformer.updateAllTypes(units);
    
    return transformer.idToCanonicalMap.values();
  }
  
  private RecordTypeDefCanonicalizer(List<GimpleCompilationUnit> units) {
    
    // Make a mapping of ALL GimpleRecordTypes emitted by GCC.
    // A struct that is defined in a header file and referenced by multiple compilation units
    // will be DUPLICATED.
    
    for (GimpleCompilationUnit unit : units) {
      for (GimpleRecordTypeDef typeDef : unit.getRecordTypes()) {
        idMap.put(typeDef.getId(), typeDef);
      }
    }
    
    // Calculate a KEY for each GimpleRecordTypeDef composed of the
    // record field's names and types.

    for (GimpleRecordTypeDef typeDef : idMap.values()) {
      String key = key(typeDef);
      GimpleRecordTypeDef canonicalDef = keyMap.get(key);
      
      if(canonicalDef == null) {
        // Never seen this type before, use as the canonical version
        canonicalDef = typeDef;
        keyMap.put(key, canonicalDef);
      }
      
      idToCanonicalMap.put(typeDef.getId(), canonicalDef);
      
      System.out.println("CANONICAL RECORD: " + typeDef.getId() + " => " + canonicalDef.getId() + 
          " [" + key + "]");
    }
  }


  public String key(GimpleRecordTypeDef typeDef) {
    StringBuilder key = new StringBuilder();
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
      appendTypeKeyTo(gimpleField.getType(), key);
      needsComma = true;
    }
  }
  
  private void appendTypeKeyTo(GimpleType type, StringBuilder key) {
    if(type instanceof GimpleRecordType) {
      GimpleRecordType recordType = (GimpleRecordType) type;
      GimpleRecordTypeDef recordTypeDef = idMap.get(recordType.getId());
      appendKeyTo(recordTypeDef, key);
    
    } else if(type instanceof GimpleIndirectType) {
      key.append("*");
      appendTypeKeyTo(type.getBaseType(), key);
    
    } else if(type instanceof GimpleArrayType) {
      key.append("[");
      appendTypeKeyTo(((GimpleArrayType) type).getComponentType(), key);
      
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

  /**
   * Resolves a type reference to the canonical version of the {@code GimpleRecordTypeDef}
   */
  public GimpleRecordTypeDef resolve(GimpleRecordType recordType) {
    GimpleRecordTypeDef canonicalDef = idToCanonicalMap.get(recordType.getId());
    if(canonicalDef == null) {
      throw new IllegalArgumentException("No such record: " + recordType.getId());
    }
    return canonicalDef;
  }

  private void updateAllTypes(List<GimpleCompilationUnit> units) {

    // Ensure that canonical type defs reference other canonical type defs in their fields
    for (GimpleRecordTypeDef recordTypeDef : idToCanonicalMap.values()) {
      updateFieldTypes(recordTypeDef);
    }
    
    // Replace ids in each compilation unit with references to the canonical type
    for (GimpleCompilationUnit unit : units) {

      for (int i = 0; i < unit.getRecordTypes().size(); i++) {
        GimpleRecordTypeDef recordTypeDef = unit.getRecordTypes().get(i);
        GimpleRecordTypeDef canonicalDef = idToCanonicalMap.get(recordTypeDef.getId());
        
        unit.getRecordTypes().set(i, canonicalDef);
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
          for (GimpleIns gimpleIns : basicBlock.getInstructions()) {
            updateTypes(gimpleIns);
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
      if(canonicalDef == null) {
        throw new IllegalStateException();
      }
      recordType.setId(canonicalDef.getId());
    
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


  private void updateTypes(GimpleIns gimpleIns) {
    if(gimpleIns instanceof GimpleReturn) {
      updateTypes(((GimpleReturn) gimpleIns).getValue());
    } else if(gimpleIns instanceof GimpleAssign) {
      GimpleAssign assignment = (GimpleAssign) gimpleIns;
      updateTypes(assignment.getLHS());
      updateTypes(assignment.getOperands());
    } else if(gimpleIns instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) gimpleIns;
      updateTypes(call.getLhs());
      updateTypes(call.getArguments());
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
        updateTypes(((GimpleComponentRef) expr).getValue());
      } else if (expr instanceof GimpleArrayRef) {
        updateTypes(((GimpleArrayRef) expr).getArray());
      } else if (expr instanceof GimpleNopExpr) {
        updateTypes(((GimpleNopExpr) expr).getValue());
      }
    }
  }
}
