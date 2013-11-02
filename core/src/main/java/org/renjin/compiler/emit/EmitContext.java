package org.renjin.compiler.emit;


import com.google.common.collect.Maps;
import org.objectweb.asm.Label;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;

import java.util.Map;

public class EmitContext {

  private VariableMap variableMap;

  private Map<IRLabel, Label> labels = Maps.newHashMap();
  private Map<LValue, Integer> localVariableIndexMap = Maps.newHashMap();
  private int nextLocalVariable = 0;

  public EmitContext(VariableMap variableMap) {
    this.variableMap = variableMap;
  }
  
  public Label getAsmLabel(IRLabel irLabel) {
    Label asmLabel = labels.get(irLabel);
    if(asmLabel == null) {
      asmLabel = new Label();
      labels.put(irLabel, asmLabel);
    }
    return asmLabel;
  }

  public void getType(Expression rhs) {

  }

  public int getLocalVariableIndex(LValue lhs, Class type) {
    if(!localVariableIndexMap.containsKey(lhs)) {
      localVariableIndexMap.put(lhs, nextLocalVariable);
      nextLocalVariable += numSlots(type);
    }
    return localVariableIndexMap.get(lhs);
  }

  private int numSlots(Class type) {
    if(type.equals(double.class) || type.equals(long.class)) {
      return 2;
    } else {
      return 1;
    }
  }
}
