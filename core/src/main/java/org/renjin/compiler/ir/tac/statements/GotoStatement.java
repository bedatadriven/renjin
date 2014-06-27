package org.renjin.compiler.ir.tac.statements;

import java.util.Arrays;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.NullExpression;


public class GotoStatement implements Statement, BasicBlockEndingStatement {

  private final IRLabel target;

  public GotoStatement(IRLabel target) {
    this.target = target;
  }

  public IRLabel getTarget() {
    return target;
  }

  
  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Arrays.asList(target);
  }

  @Override
  public String toString() {
    return "goto " + target;
  }


  @Override
  public Expression getRHS() {
    return NullExpression.INSTANCE;
  }

  @Override
  public void setRHS(Expression newRHS) {
    if(newRHS != NullExpression.INSTANCE) {
      throw new IllegalArgumentException();
    }
  }


  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitGoto(this);
  }

  @Override
  public int emit(EmitContext emitContext, MethodVisitor mv) {
    mv.visitJumpInsn(Opcodes.GOTO, emitContext.getAsmLabel(target));
    return 0;
  }
}
