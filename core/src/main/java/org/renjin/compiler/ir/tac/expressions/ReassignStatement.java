package org.renjin.compiler.ir.tac.expressions;


import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.eval.Context;
import org.renjin.primitives.special.ReassignLeftFunction;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

import java.util.Collections;

public class ReassignStatement extends Statement {

  private String lhs;
  private Expression rhs;

  public ReassignStatement(String lhs, Expression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public Expression getRHS() {
    return rhs;
  }

  @Override
  public void emit(EmitContext emitContext, InstructionAdapter mv) {
    mv.visitVarInsn(Opcodes.ALOAD, emitContext.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, emitContext.getEnvironmentVarIndex());

    mv.aconst(lhs);
    rhs.getCompiledExpr(emitContext).loadSexp(emitContext, mv);

    mv.invokestatic(Type.getInternalName(ReassignLeftFunction.class), "reassign",
        Type.getMethodDescriptor(Type.VOID_TYPE,
            Type.getType(Context.class),
            Type.getType(Environment.class),
            Type.getType(String.class),
            Type.getType(SEXP.class)), false);
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      rhs = child;
    } else {
      throw new IllegalArgumentException();
    }
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
  public String toString() {
    return lhs + " <<- " + rhs;
  }
}
