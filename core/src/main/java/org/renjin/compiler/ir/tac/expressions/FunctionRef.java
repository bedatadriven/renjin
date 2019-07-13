package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Function;
import org.renjin.sexp.Symbol;

import java.util.Objects;

public class FunctionRef extends Variable {

  private final Symbol symbol;

  public FunctionRef(Symbol symbol) {
    this.symbol = symbol;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public String toString() {
    return symbol + "*";
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
        mv.aconst(symbol.getPrintName());
        mv.invokevirtual(Type.getInternalName(Context.class), "evaluateFunction",
            Type.getMethodDescriptor(Type.getType(Function.class),
                Type.getType(Environment.class),
                Type.getType(String.class)), false);
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FunctionRef that = (FunctionRef) o;
    return symbol.equals(that.symbol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbol);
  }
}
