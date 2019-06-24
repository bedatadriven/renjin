package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.eval.Context;
import org.renjin.primitives.special.DollarAssignFunction;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

/**
 * The expression {@code x$a}
 */
public class NamedElementUpdate implements Expression {
  private Expression object;
  private FunctionCall call;
  private String name;
  private Expression rhs;

  public NamedElementUpdate(Expression object, FunctionCall call, String name, Expression rhs) {
    this.object = object;
    this.call = call;
    this.name = name;
    this.rhs = rhs;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    object.updateTypeBounds(typeMap);
    rhs.updateTypeBounds(typeMap);
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

        // SEXP assign(Context context, Environment rho, FunctionCall call, SEXP object, String name, SEXP value)
        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

        emitContext.constantSexp(call).loadSexp(context, mv);
        mv.checkcast(Type.getType(FunctionCall.class));

        object.getCompiledExpr(context).loadSexp(context, mv);
        mv.aconst(name);
        rhs.getCompiledExpr(context).loadSexp(context, mv);

        mv.invokestatic(Type.getInternalName(DollarAssignFunction.class), "assign",
            Type.getMethodDescriptor(Type.getType(SEXP.class),
                Type.getType(Context.class),
                Type.getType(Environment.class),
                Type.getType(FunctionCall.class),
                Type.getType(SEXP.class),
                Type.getType(String.class),
                Type.getType(SEXP.class)), false);
      }
    };
  }

  @Override
  public int getChildCount() {
    return 2;
  }

  @Override
  public Expression childAt(int index) {
    switch (index) {
      case 0:
        return object;
      case 1:
        return rhs;
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    switch (childIndex) {
      case 0:
        object = child;
        break;
      case 1:
        rhs = child;
        break;
      default:
        throw new IllegalArgumentException();
    }
  }
}
