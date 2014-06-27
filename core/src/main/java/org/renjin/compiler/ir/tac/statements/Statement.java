package org.renjin.compiler.ir.tac.statements;

import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tree.TreeNode;
import org.renjin.eval.Context;


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
  int emit(EmitContext emitContext, MethodVisitor mv);

}