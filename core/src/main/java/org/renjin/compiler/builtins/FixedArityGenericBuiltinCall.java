package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

import java.util.List;

/**
 * Calls a generic builtin with a fixed number of arguments.
 */
public class FixedArityGenericBuiltinCall implements Specialization {
  private final String internalName;
  private final String methodName;

  public FixedArityGenericBuiltinCall(String internalName, String methodName) {
    this.internalName = internalName;
    this.methodName = methodName;
  }

  @Override
  public ValueBounds getResultBounds() {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, FunctionCall call, List<IRArgument> arguments) {

    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {

        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
        context.constantSexp(call).loadSexp(context, mv);
        mv.checkcast(Type.getType(FunctionCall.class));
        for (IRArgument argument : arguments) {
          argument.getExpression().getCompiledExpr(context).loadSexp(context, mv);
        }

        mv.invokestatic(internalName, methodName, descriptor(arguments.size()), false);
      }
    };
  }

  private String descriptor(int arity) {


    Type[] argumentTypes = new Type[3 + arity];
    argumentTypes[0] = Type.getType(Context.class);
    argumentTypes[1] = Type.getType(Environment.class);
    argumentTypes[2] = Type.getType(FunctionCall.class);
    for (int i = 0; i < arity; i++) {
      argumentTypes[3 + i] = Type.getType(SEXP.class);
    }

    return Type.getMethodDescriptor(Type.getType(SEXP.class), argumentTypes);
  }
}
