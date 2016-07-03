package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.commons.InstructionAdapter;


public interface Statement extends TreeNode {

  Iterable<IRLabel> possibleTargets();

  Expression getRHS();

  void setRHS(Expression newRHS);

  void accept(StatementVisitor visitor);

  /**
   * Emits the bytecode for this instruction
   * @param emitContext
   * @param mv
   * @return the required increase to the stack
   */
  int emit(EmitContext emitContext, InstructionAdapter mv);
  
}