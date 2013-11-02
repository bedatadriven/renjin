package org.renjin.compiler.ir.tac.statements;

import java.util.Collections;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.IRUtils;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.sexp.Vector;


public class Assignment implements Statement {
  private LValue lhs;
  private Expression rhs;
 
  public Assignment(LValue lhs, Expression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
 
  public LValue getLHS() {
    return lhs;
  }
 
  @Override
  public Expression getRHS() {
    return rhs;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }
  

  @Override
  public void setRHS(Expression newRHS) {
    this.rhs = newRHS;
  }

  @Override
  public String toString() {
    return getLHS() + " " + IRUtils.LEFT_ARROW + " "  + rhs;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return rhs;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      rhs = child;
    } else {
      throw new IllegalArgumentException("childIndex=" + childIndex);
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitAssignment(this);
  }

  @Override
  public void emit(EmitContext emitContext, MethodVisitor mv) {
    rhs.emitPush(emitContext, mv);

    Class rhsType = rhs.inferType();

    int localVariable = emitContext.getLocalVariableIndex(lhs, rhsType);
    if(rhsType.equals(double.class)) {
      mv.visitVarInsn(Opcodes.DSTORE, localVariable);
    } else if(Vector.class.isAssignableFrom(rhsType)) {
      mv.visitVarInsn(Opcodes.ASTORE, localVariable);
    } else {
      throw new UnsupportedOperationException("don't know how to STORE " + rhsType);
    }
  }

  public void setLHS(LValue lhs) {
    this.lhs = lhs;
  }
}
