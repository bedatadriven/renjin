package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

public class PromiseExpr implements Expression {

  private final ValueBounds bounds;
  private final SEXP sexp;

  public PromiseExpr(SEXP sexp) {
    this.sexp = sexp;
    this.bounds = ValueBounds.PROMISED;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    return bounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return bounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        context.constantSexp(sexp).loadSexp(context, mv);
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
        mv.invokeinterface(Type.getInternalName(SEXP.class), "promise", Type.getMethodDescriptor(
            Type.getType(SEXP.class),
            Type.getType(Environment.class)));
      }
    };
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "promise(" + sexp + ")";
  }
}
