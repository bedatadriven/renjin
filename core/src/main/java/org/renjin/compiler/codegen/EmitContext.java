/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.codegen;


import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.LiveSet;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.LocalVarAllocator;
import org.renjin.compiler.codegen.var.VariableMap;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Multimap;
import org.renjin.sexp.Symbol;

import java.util.Map;

public class EmitContext {

  private final LiveSet liveSet;
  private final LocalVarAllocator localVariables;
  private final Type returnType;
  private final VariableMap variableMap;
  private Map<IRLabel, Label> labels = Maps.newHashMap();
  private Multimap<LValue, Expression> definitionMap = HashMultimap.create();

  private int loopVectorIndex;
  private int loopIterationIndex;

  public EmitContext(ControlFlowGraph cfg, LiveSet liveSet,
                     TypeSolver types, LocalVarAllocator localVariables, Type returnType) {
    this.liveSet = liveSet;
    this.localVariables = localVariables;
    this.returnType = returnType;
    this.variableMap = new VariableMap(localVariables, types);
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

  public Type getReturnType() {
    return returnType;
  }

  public Label getAsmLabel(IRLabel irLabel) {
    Label asmLabel = labels.get(irLabel);
    if(asmLabel == null) {
      asmLabel = new Label();
      labels.put(irLabel, asmLabel);
    }
    return asmLabel;
  }

  /**
   * Creates a new {@code EmitContext} that allocates local variables in the same context, and overrides
   * {@link #writeReturn(InstructionAdapter, CompiledSexp)} to jump to the end of the inlined function rather than
   * actually returning.
   */
  public InlineEmitContext inlineContext(ControlFlowGraph cfg, TypeSolver types, LiveSet liveSet, VariableStrategy returnVariable) {
    return new InlineEmitContext(cfg, liveSet, types, localVariables, returnVariable);
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

  public int getLocalVariableCount() {
    return localVariables.getCount();
  }

  public void writeReturn(InstructionAdapter mv, CompiledSexp returnExpr) {
    returnExpr.loadSexp(this, mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  public void writeDone(InstructionAdapter mv) {

  }

  public CompiledSexp getParamExpr(Symbol paramName) {
    throw new IllegalStateException();
  }

  public VariableStrategy getVariable(LValue lhs) {
    return variableMap.getStorage(lhs);
  }

}
