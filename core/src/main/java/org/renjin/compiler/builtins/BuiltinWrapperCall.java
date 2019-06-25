package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.ArgListGen;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.ArgList;
import org.renjin.eval.Context;
import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

import java.util.List;

public class BuiltinWrapperCall implements Specialization {
  private final String name;
  private final int forwardedArgumentIndex;

  public BuiltinWrapperCall(String name, int forwardedArgumentIndex) {
    this.name = name;
    this.forwardedArgumentIndex = forwardedArgumentIndex;
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

    // Invoke one of the static wrappers in the generated Builtin
    // Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] evaluatedArguments

    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {

        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

        ArgListGen argListGen = new ArgListGen(context, mv)
            .names(arguments.stream().map(a -> a.isNamed() ? a.getName() : null))
            .values(arguments.stream().map(a -> a.getExpression().getCompiledExpr(emitContext)));

        if(forwardedArgumentIndex == -1) {
          argListGen.load();

        } else {
          // Need Context and Environment on the
          // stack for ArgList.forceExpand()
          mv.dup2();
          argListGen.forceExpandLoad(forwardedArgumentIndex);
        }

        emitContext.constantSexp(call).loadSexp(emitContext, mv);
        mv.checkcast(Type.getType(FunctionCall.class));

        mv.invokestatic("org/renjin/primitives/Builtins", WrapperGenerator2.toJavaName("", name),
            Type.getMethodDescriptor(Type.getType(SEXP.class),
                Type.getType(Context.class),
                Type.getType(Environment.class),
                Type.getType(ArgList.class),
                Type.getType(FunctionCall.class)), false);
      }
    };
  }
}
