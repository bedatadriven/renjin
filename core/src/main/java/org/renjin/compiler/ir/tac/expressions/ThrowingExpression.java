package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

public class ThrowingExpression implements Expression {

  private final String message;

  public ThrowingExpression(String message) {
    this.message = message;
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        mv.anew(Type.getType(EvalException.class));
        mv.dup();
        mv.aconst(message);
        mv.invokespecial(Type.getInternalName(EvalException.class), "<init>",
            Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)), false);
      }
    };
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
}
