package org.renjin.compiler.emit;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.util.Map;

public class EmitContext {

  private Map<IRLabel, Label> labels = Maps.newHashMap();
  private Multimap<LValue, Expression> definitionMap = HashMultimap.create();
  private VariableSlots variableSlots;

  public EmitContext(ControlFlowGraph cfg, VariableSlots variableSlots) {
    this.variableSlots = variableSlots;
    buildDefinitionMap(cfg);
  }
  
  public int getEnvironmentVarIndex() {
    return 2;
  }

  private void buildDefinitionMap(ControlFlowGraph cfg) {
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      for(Statement stmt : bb.getStatements()) {
        if(stmt instanceof Assignment) {
          Assignment assignment = (Assignment)stmt;
          definitionMap.put(assignment.getLHS(), assignment.getRHS());
        }
      }
    }
  }

  public Label getAsmLabel(IRLabel irLabel) {
    Label asmLabel = labels.get(irLabel);
    if(asmLabel == null) {
      asmLabel = new Label();
      labels.put(irLabel, asmLabel);
    }
    return asmLabel;
  }

  public int getRegister(LValue lValue) {
    return variableSlots.getSlot(lValue);
  }

  public int convert(InstructionAdapter mv, Type fromType, Type toType) {
    if(fromType.equals(toType)) {
      // NOOP
      return 0;

    } else if(fromType.getSort() != Type.OBJECT && toType.getSort() != Type.OBJECT) {
      // Simple primitive conversion
      mv.cast(fromType, toType);
      return 0;
      
    } else if(fromType.equals(Type.getType(SEXP.class))) {
      // FROM SEXP -> .....
      if (toType.getSort() == Type.OBJECT) {
        mv.checkcast(toType);
        return 0;

      } else if (toType.equals(Type.DOUBLE_TYPE)) {
        mv.invokevirtual(Type.getInternalName(SEXP.class), "asReal",
            Type.getMethodDescriptor(Type.DOUBLE_TYPE), false);
        return 0;

      } else if (toType.equals(Type.INT_TYPE)) {
        mv.checkcast(Type.getType(Vector.class));
        mv.iconst(0);
        mv.invokestatic(Type.getInternalName(Vector.class), "getElementAsInt",
            Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE), false);
        return 1;

      }
    }
    
    throw new UnsupportedOperationException("Unsupported conversion: " + fromType + " -> " + toType);
  }

  public VariableStorage getVariableStorage(LValue lhs) {
    return variableSlots.getStorage(lhs);
  }
}
