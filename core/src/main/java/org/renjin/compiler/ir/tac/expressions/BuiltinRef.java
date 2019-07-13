package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Function;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Objects;

public class BuiltinRef implements Expression {

  private final PrimitiveFunction builtin;
  private final ValueBounds bounds;

  public BuiltinRef(Symbol symbol) {
    builtin = Primitives.getBuiltin(symbol);
    bounds = ValueBounds.constantValue(builtin);
  }

  public BuiltinRef(PrimitiveFunction function) {
    builtin = function;
    bounds = ValueBounds.constantValue(builtin);
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
        String wrapperName = WrapperGenerator2.toFullJavaName(builtin.getName()).replace('.', '/');
        mv.getstatic(wrapperName, "INSTANCE", "L" + wrapperName + ";");
      }

      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv, Type type) {
        loadSexp(context, mv);
        if(!type.equals(Type.getType(SEXP.class)) &&
            !type.equals(Type.getType(Function.class))) {
          mv.checkcast(type);
        }
      }
    };
  }



  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }


  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BuiltinRef that = (BuiltinRef) o;
    return builtin.equals(that.builtin);
  }

  @Override
  public int hashCode() {
    return Objects.hash(builtin);
  }

  @Override
  public String toString() {
    return builtin.getName();
  }
}
