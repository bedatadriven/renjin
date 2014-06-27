package org.renjin.compiler.ir.tac.statements;

import java.util.Collections;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.SEXP;


public class ReturnStatement implements Statement, BasicBlockEndingStatement {

  private Expression value;

  public ReturnStatement(Expression value) {
    super();
    this.value = value;
  }
  
  public Expression getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return "return " + value;
  }

  @Override
  public Expression getRHS() {
    return value;
  }
  
  @Override
  public void setRHS(Expression newRHS) {
    this.value = newRHS;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return value;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      value = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitReturn(this);
  }

  @Override
  public int emit(EmitContext emitContext, MethodVisitor mv) {
    Class type = getRHS().getType();
    int stackSizeIncrease = 0;
    if(type.equals(double.class)) {
      stackSizeIncrease = getRHS().emitPush(emitContext, mv);
      mv.visitMethodInsn(Opcodes.INVOKESTATIC,
          Type.getInternalName(DoubleArrayVector.class), "valueOf",
          Type.getMethodDescriptor(Type.getType(DoubleArrayVector.class), Type.DOUBLE_TYPE));
   //   mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(SEXP.class));

    } else {
      throw new UnsupportedOperationException();
    }

    mv.visitInsn(Opcodes.ARETURN);
    return stackSizeIncrease;
  }
}
