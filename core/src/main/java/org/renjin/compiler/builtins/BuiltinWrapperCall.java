package org.renjin.compiler.builtins;

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

import java.util.ArrayList;
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

        if(forwardedArgumentIndex == -1) {
          loadFixedArgList(mv);

        } else if(arguments.size() == 0 && forwardedArgumentIndex == 0) {
          mv.dup2();
          mv.invokestatic(Type.getInternalName(ArgList.class), "forceExpand0",
              Type.getMethodDescriptor(Type.getType(ArgList.class),
                  Type.getType(Context.class),
                  Type.getType(Environment.class)), false);

        } else if(arguments.size() == 1 && forwardedArgumentIndex == 1) {

          mv.dup2();
          mv.aconst(arguments.get(0).getName());
          arguments.get(0).getExpression().getCompiledExpr(emitContext).loadSexp(context, mv);
          mv.invokestatic(Type.getInternalName(ArgList.class), "forceExpand1",
              Type.getMethodDescriptor(Type.getType(ArgList.class),
                  Type.getType(Context.class),
                  Type.getType(Environment.class),
                  Type.getType(String.class),
                  Type.getType(SEXP.class)), false);



        } else {
          loadForcedAndExpanded(mv);
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

      private void loadForcedAndExpanded(InstructionAdapter mv) {
        mv.dup2();
        List<Type> argumentTypes = new ArrayList<>();
        argumentTypes.add(Type.getType(Context.class));
        argumentTypes.add(Type.getType(Environment.class));
        for (IRArgument argument : arguments) {
          argumentTypes.add(Type.getType(String.class));
          argumentTypes.add(Type.getType(SEXP.class));

          mv.aconst(argument.getName());
          argument.getExpression().getCompiledExpr(emitContext).loadSexp(emitContext, mv);
        }

        argumentTypes.add(Type.INT_TYPE);
        mv.iconst(forwardedArgumentIndex);

        mv.invokestatic(Type.getInternalName(ArgList.class), "forceExpand",
            Type.getMethodDescriptor(Type.getType(ArgList.class), argumentTypes.toArray(new Type[0])), false);
      }

      private void loadFixedArgList(InstructionAdapter mv) {
        if(arguments.size() <= 5) {
          loadFixedArgListWithHelper(mv);
        } else {
          loadFixedArgListArray(mv);
        }
      }

      private void loadFixedArgListArray(InstructionAdapter mv) {
        int numArguments = arguments.size();
        mv.iconst(numArguments);
        mv.newarray(Type.getType(String.class));
        for (int i = 0; i < arguments.size(); i++) {
          if(arguments.get(i).isNamed()) {
            mv.dup();
            mv.iconst(i);
            mv.aconst(arguments.get(i).getName());
            mv.visitInsn(Opcodes.AASTORE);
          }
        }

        mv.iconst(numArguments);
        mv.newarray(Type.getType(SEXP.class));
        for (int i = 0; i < arguments.size(); i++) {
          mv.dup();
          mv.iconst(i);
          arguments.get(i).getExpression().getCompiledExpr(emitContext).loadSexp(emitContext, mv);
          mv.visitInsn(Opcodes.AASTORE);
        }
        mv.invokestatic(Type.getInternalName(ArgList.class), "of",
            Type.getMethodDescriptor(Type.getType(ArgList.class),
                Type.getType(String[].class),
                Type.getType(SEXP[].class)), false);
      }

      private void loadFixedArgListWithHelper(InstructionAdapter mv) {
        List<Type> argumentTypes = new ArrayList<>();
        for (IRArgument argument : arguments) {
          argumentTypes.add(Type.getType(String.class));
          argumentTypes.add(Type.getType(SEXP.class));

          mv.aconst(argument.getName());
          argument.getExpression().getCompiledExpr(emitContext).loadSexp(emitContext, mv);
        }

        mv.invokestatic(Type.getInternalName(ArgList.class), "of",
            Type.getMethodDescriptor(Type.getType(ArgList.class), argumentTypes.toArray(new Type[0])), false);
      }

    };
  }
}
