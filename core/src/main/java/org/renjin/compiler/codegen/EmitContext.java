package org.renjin.compiler.codegen;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

import java.util.Map;

public class EmitContext {

  private Map<IRLabel, Label> labels = Maps.newHashMap();
  private Multimap<LValue, Expression> definitionMap = HashMultimap.create();
  private int paramSize;
  private VariableSlots variableSlots;
  
  private int loopVectorIndex;
  private int loopIterationIndex;
  
  private int maxInlineVariables = 0;
  
  private Map<Symbol, InlineParamExpr> inlinedParameters = Maps.newHashMap();

  public EmitContext(ControlFlowGraph cfg, int paramSize, VariableSlots variableSlots) {
    this.paramSize = paramSize;
    this.variableSlots = variableSlots;
    buildDefinitionMap(cfg);
  }

  public int getContextVarIndex() {
    return 1;
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
  
  public EmitContext inlineContext(ControlFlowGraph cfg, TypeSolver types) {
    
    VariableSlots childSlots = new VariableSlots(paramSize + variableSlots.getNumLocals(), types);
    if(childSlots.getNumLocals() > maxInlineVariables) {
      maxInlineVariables = childSlots.getNumLocals();
    }
    
    EmitContext childContext = new EmitContext(cfg, paramSize + this.variableSlots.getNumLocals(), childSlots);
    
    return childContext;
  }
  
  public void setInlineParameter(Symbol parameterName, InlineParamExpr value) {
    inlinedParameters.put(parameterName, value);
  }


  public InlineParamExpr getInlineParameter(Symbol param) {
    InlineParamExpr paramExpr = inlinedParameters.get(param);
    if(paramExpr == null) {
      throw new IllegalStateException("No expression set for parameter " + param);
    }
    return paramExpr;
  }
  

  public int getLoopVectorIndex() {
    return loopVectorIndex;
  }

  public void setLoopVectorIndex(int loopVectorIndex) {
    this.loopVectorIndex = loopVectorIndex;
  }

  public int getLoopIterationIndex() {
    return loopIterationIndex;
  }

  public void setLoopIterationIndex(int loopIterationIndex) {
    this.loopIterationIndex = loopIterationIndex;
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
        mv.invokeinterface(Type.getInternalName(SEXP.class), "asReal",
            Type.getMethodDescriptor(Type.DOUBLE_TYPE));
        return 0;

      } else if (toType.equals(Type.INT_TYPE)) {
        mv.checkcast(Type.getType(Vector.class));
        mv.iconst(0);
        mv.invokeinterface(Type.getInternalName(Vector.class), "getElementAsInt",
            Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE));
        return 1;

      }
    } else if(toType.equals(Type.getType(SEXP.class))) {
      // TO SEXP --->
      
      if(fromType.getSort() == Type.OBJECT) {
        // No cast necessary
        return 0;
      }
      
      switch (fromType.getSort()) {
        case Type.INT:
          return box(mv, IntVector.class, Type.INT_TYPE);
        
        case Type.DOUBLE:
          return box(mv, DoubleVector.class, Type.DOUBLE_TYPE);
        
      }
      
      
    }
    
    throw new UnsupportedOperationException("Unsupported conversion: " + fromType + " -> " + toType);
  }
  
  private int box(InstructionAdapter mv, Class vectorClass, Type primitiveType) {
    mv.invokestatic(Type.getInternalName(vectorClass), "valueOf",
        Type.getMethodDescriptor(Type.getType(vectorClass), primitiveType), false);
    return 0;
  }

  public VariableStorage getVariableStorage(LValue lhs) {
    return variableSlots.getStorage(lhs);
  }

  public int getLocalVariableCount() {
    return paramSize + variableSlots.getNumLocals() + maxInlineVariables;
  }

}
