package org.renjin.compiler.emit;

import org.objectweb.asm.Type;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.sexp.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps variables to their slot indexes o
 */
public class VariableSlots {
  
  private final Map<LValue, VariableStorage> storage = new HashMap<>();
  private int nextSlot;
  
  public VariableSlots(int parameterSize, TypeSolver types) {

    nextSlot = parameterSize;
    
    for (Map.Entry<LValue, ValueBounds> entry : types.getVariables().entrySet()) {
      LValue variable = entry.getKey();
      ValueBounds bounds = entry.getValue();
    
      storage.put(variable, computeStorage(bounds));
    }
  }

  private VariableStorage computeStorage(ValueBounds bounds) {
    Type type = computeType(bounds);
    int slotIndex = nextSlot;
    nextSlot += type.getSize();
    
    return new VariableStorage(slotIndex, type);
  }

  private Type computeType(ValueBounds bounds) {
    if(bounds.getTypeSet() == TypeSet.DOUBLE) {
      if(bounds.getLength() == 1) {
        return Type.DOUBLE_TYPE;
      } else {
        return Type.getType(DoubleVector.class);
      }
    } else if(bounds.getTypeSet() == TypeSet.INT || 
              bounds.getTypeSet() == TypeSet.LOGICAL) {
      if(bounds.getLength() == 1) {
        return Type.INT_TYPE;
      } else {
        return Type.getType(IntVector.class);
      } 
    } else if(bounds.getTypeSet() == TypeSet.LOGICAL) {
      if(bounds.getLength() == 1) {
        return Type.INT_TYPE;
      } else {
        return Type.getType(LogicalVector.class);
      }
    } else if(bounds.getTypeSet() == TypeSet.RAW) {
      if(bounds.getLength() == 1) {
        return Type.BYTE_TYPE;
      } else {
        return Type.getType(RawVector.class);
      }
    } else if(bounds.getTypeSet() == TypeSet.STRING) {
      if (bounds.getLength() == 1) {
        return Type.getType(String.class);
      } else {
        return Type.getType(StringVector.class);
      }
    } else {
      return Type.getType(SEXP.class);
    }
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (Map.Entry<LValue, VariableStorage> entry : this.storage.entrySet()) {
      s.append(entry.getKey()).append(" => ").append(entry.getValue()).append("\n");
    }
    return s.toString();
  }
}
