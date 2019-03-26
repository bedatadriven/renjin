/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.compiler.builtins;

import org.apache.commons.math.complex.Complex;
import org.renjin.compiler.codegen.BytecodeTypes;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.NoAttributes;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.SEXP;

import java.util.*;

import static org.renjin.compiler.ir.ValueBounds.LENGTH_ONE;
import static org.renjin.compiler.ir.ValueBounds.MAYBE_ATTRIBUTES;


/**
 * Call to a single static method overload of a builtin.
 */
public class StaticMethodCall implements Specialization {

  private final JvmMethod method;
  private final ValueBounds resultBounds;
  private final boolean pure;
  private final boolean varArgs;

  public StaticMethodCall(JvmMethod method) {
    this(method, boundsOf(method));
  }

  public StaticMethodCall(JvmMethod method, ValueBounds bounds) {
    this.method = method;
    this.pure = method.isPure();
    this.resultBounds = bounds;
    this.varArgs = method.acceptsArgumentList();
  }


  static ValueBounds boundsOf(JvmMethod method) {
    Class returnType = method.getReturnType();

    ValueBounds.Builder builder = new ValueBounds.Builder();
    builder.setTypeSet(TypeSet.of(returnType));
    if(returnType.isPrimitive() ||
        returnType.equals(String.class) || returnType.equals(Logical.class) || returnType.equals(Complex.class)) {
      builder.addFlags(LENGTH_ONE);
    }

    if(SEXP.class.isAssignableFrom(returnType)) {
      if(method.isAnnotatedWith(NoAttributes.class)) {
        // Nothing! nice.
      } else {
        builder.addFlags(MAYBE_ATTRIBUTES);
      }
    }

    return builder.build();
  }

  public Specialization furtherSpecialize(List<ValueBounds> argumentBounds) {
    if (pure && ValueBounds.allConstant(argumentBounds)) {
      return ConstantCall.evaluate(method, argumentBounds);
    }
    return this;
  }

  public ValueBounds getResultBounds() {
    return resultBounds;
  }

  @Override
  public boolean isPure() {
    return method.isPure();
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {


    if(SEXP.class.isAssignableFrom(method.getReturnType())) {
      return new SexpExpr() {
        @Override
        public void loadSexp(EmitContext context, InstructionAdapter mv) {
          invoke(context, mv, arguments);
        }
      };

    } else if(method.getReturnType().isPrimitive() || method.getReturnType().equals(String.class)) {
      return new ScalarExpr(VectorType.fromJvmType(method.getReturnType())) {
        @Override
        public void loadScalar(EmitContext context, InstructionAdapter mv) {
          invoke(context, mv, arguments);
        }
      };
    } else if(method.getReturnType().equals(Logical.class)) {
      return new ScalarExpr(VectorType.LOGICAL) {
        @Override
        public void loadScalar(EmitContext context, InstructionAdapter mv) {
          invoke(context, mv, arguments);
          mv.invokevirtual(Type.getInternalName(Logical.class), "getInternalValue", "()I", false);
        }
      };
    } else {
      throw new UnsupportedOperationException("returnType: " + method.getReturnType());
    }
  }

  private void invoke(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {

    if(varArgs) {
      invokeVarArgs(emitContext, mv, arguments);
    } else {
      invokeSimple(emitContext, mv, arguments);
    }
  }

  private void invokeVarArgs(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {

    Set<String> namedFlags = method.namedFlags();

    Map<String, IRArgument> flags = new HashMap<>();

    Iterator<IRArgument> actualIt = arguments.iterator();
    Iterator<JvmMethod.Argument> formalIt = method.getAllArguments().iterator();

    JvmMethod.Argument formal;

    while(formalIt.hasNext()) {
      formal = formalIt.next();

      if (formal.isContextual()) {

        // Contextual arguments like Context, Environment, etc

        loadContextArgument(emitContext, mv, formal);

      } else if (formal.isVarArg()) {

        // If we've reached the @ArgumentList parameter, then collect the rest of the
        // arguments into an argument list and zero or more named flags

        List<IRArgument> argumentList = new ArrayList<>();

        while (actualIt.hasNext()) {
          IRArgument argument = actualIt.next();
          if (argument.isNamed() && namedFlags.contains(argument.getName())) {
            flags.put(argument.getName(), argument);
          } else {
            argumentList.add(argument);
          }
        }

        loadArgumentList(emitContext, mv, argumentList);

      } else if (formal.isNamedFlag()) {

        IRArgument flag = flags.get(formal.getName());
        if (flag == null) {
          mv.visitLdcInsn(formal.getDefaultValue() ? 1 : 0);
        } else {
          throw new UnsupportedOperationException("TODO");
        }
      } else {

        // Normal positional argument

        IRArgument argument = actualIt.next();
        argument.getExpression().getCompiledExpr(emitContext).loadAsArgument(emitContext, mv, formal.getClazz());

      }
    }

    invokeMethod(mv);
  }

  private void loadContextArgument(EmitContext emitContext, InstructionAdapter mv, JvmMethod.Argument argument) {
    if(argument.getClazz().equals(Context.class)) {
      mv.visitVarInsn(Opcodes.ALOAD, emitContext.getContextVarIndex());
    } else {
      throw new UnsupportedOperationException("Context argument: " + argument.getClazz().getName());
    }
  }

  private void loadArgumentList(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> argumentList) {
    if(argumentList.isEmpty()) {
      mv.getstatic(Type.getInternalName(ListVector.class), "EMPTY", Type.getDescriptor(ListVector.class));

    } else if(argumentList.size() == 1) {
      argumentList.get(0).getExpression().getCompiledExpr(emitContext).loadSexp(emitContext, mv);
      mv.invokestatic(Type.getInternalName(ListVector.class), "of", Type.getMethodDescriptor(
          Type.getType(ListVector.class), BytecodeTypes.SEXP_TYPE), false);

    } else if(argumentList.size() == 2) {
      argumentList.get(0).getExpression().getCompiledExpr(emitContext).loadSexp(emitContext, mv);
      argumentList.get(1).getExpression().getCompiledExpr(emitContext).loadSexp(emitContext, mv);

      mv.invokestatic(Type.getInternalName(ListVector.class), "of", Type.getMethodDescriptor(
          Type.getType(ListVector.class), BytecodeTypes.SEXP_TYPE, BytecodeTypes.SEXP_TYPE), false);

    } else {
      mv.visitLdcInsn(argumentList.size());
      mv.newarray(BytecodeTypes.SEXP_TYPE);

      for (int i = 0; i < argumentList.size(); i++) {
        mv.dup();
        mv.visitLdcInsn(i);
        argumentList.get(i).getExpression().getCompiledExpr(emitContext).loadSexp(emitContext, mv);
        mv.visitInsn(Opcodes.AASTORE);
      }
      mv.invokestatic(Type.getInternalName(ListVector.class), "of",
          "([Lorg/renjin/sexp/SEXP;)Lorg/renjin/sexp/ListVector;", false);
    }
  }

  private void invokeSimple(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    int positionalArgument = 0;
    for (JvmMethod.Argument argument : method.getAllArguments()) {
      if (argument.isContextual() || argument.isNamedFlag() || argument.isVarArg()) {
        throw new UnsupportedOperationException("TODO");
      }
      CompiledSexp compiledArg = arguments.get(positionalArgument).getExpression().getCompiledExpr(emitContext);
      compiledArg.loadAsArgument(emitContext, mv, argument.getClazz());
    }

    invokeMethod(mv);
  }

  private void invokeMethod(InstructionAdapter mv) {
    mv.invokestatic(
        Type.getInternalName(method.getDeclaringClass()),
        method.getName(),
        Type.getMethodDescriptor(method.getMethod()),
        false);
  }
}
