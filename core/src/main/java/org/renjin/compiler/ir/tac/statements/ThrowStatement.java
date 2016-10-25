package org.renjin.compiler.ir.tac.statements;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.NullExpression;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Collections;

/**
 * Statement that throws an Eval Exception
 */
public class ThrowStatement implements Statement, BasicBlockEndingStatement {

  private String message;

  public ThrowStatement(String message) {
    this.message = message;
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
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public Expression getRHS() {
    return NullExpression.INSTANCE;
  }

  @Override
  public void setRHS(Expression newRHS) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void accept(StatementVisitor visitor) {

  }

  @Override
  public int emit(EmitContext emitContext, InstructionAdapter mv) {
    mv.anew(Type.getType(EvalException.class));
    mv.visitInsn(Opcodes.DUP);
    mv.visitLdcInsn(message);
    mv.invokespecial(Type.getInternalName(EvalException.class), "<init>",
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)), false);
    mv.athrow();

    return 0;
  }

  @Override
  public String toString() {
    return "throw \"" + message + "\"";
  }
}
