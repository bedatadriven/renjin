/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.compiler.codegen.BytecodeTypes;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.Context;
import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

import java.util.Iterator;
import java.util.List;

/**
 * Specialization for builtins that are marked {@link org.renjin.invoke.annotations.DataParallel} and
 * whose arguments are "recycled" for multiple calls.
 */
public class DataParallelCall implements Specialization {

  private final String name;
  private final JvmMethod method;
  private List<ValueBounds> argumentBounds;
  private final ValueBounds resultBounds;

  public DataParallelCall(Primitives.Entry primitive, JvmMethod method, List<ValueBounds> argumentBounds) {
    this.name = primitive.name;
    this.method = method;
    this.argumentBounds = argumentBounds;
    this.resultBounds = computeBounds(argumentBounds);
  }

  private ValueBounds computeBounds(List<ValueBounds> argumentBounds) {

    List<ValueBounds> recycledArguments = recycledArgumentBounds(argumentBounds);

    ValueBounds.Builder bounds = new ValueBounds.Builder();
    bounds.setType(method.getReturnType());
    bounds.setFlag(computeFlags(argumentBounds));

    switch (method.getPreserveAttributesStyle()) {
      case NONE:
        bounds.setEmptyAttributes();
        break;
      case STRUCTURAL:
        buildStructuralBounds(bounds, recycledArguments);
        break;
      case ALL:
        buildAllBounds(bounds, recycledArguments);
        break;
    }

    return bounds.build();
  }

  private int computeFlags(List<ValueBounds> argumentBounds) {
    assert !argumentBounds.isEmpty();

    // These properties are preserved if all arguments share them
    int flags = ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_NON_ZERO_LENGTH | ValueBounds.FLAG_LENGTH_ONE;

    for (ValueBounds argumentBound : argumentBounds) {
      flags &= argumentBound.getFlags();
    }

    return flags;
  }

  /**
   * Makes a list of {@link ValueBounds} for @Recycled arguments.
   */
  private List<ValueBounds> recycledArgumentBounds(List<ValueBounds> argumentBounds) {
    List<ValueBounds> list = Lists.newArrayList();
    Iterator<ValueBounds> argumentIt = argumentBounds.iterator();
    for (JvmMethod.Argument formal : method.getFormals()) {
      if (formal.isRecycle()) {
        list.add(argumentIt.next());
      }
    }
    return list;
  }

  private void buildStructuralBounds(ValueBounds.Builder bounds, List<ValueBounds> argumentBounds) {

    for (ValueBounds argumentBound : argumentBounds) {
      if(argumentBound.attributeCouldBePresent(Symbols.DIM)) {
        bounds.attributeCouldBePresent(Symbols.DIM);
      }
      if(argumentBound.attributeCouldBePresent(Symbols.DIMNAMES)) {
        bounds.attributeCouldBePresent(Symbols.DIMNAMES);
      }
      if(argumentBound.attributeCouldBePresent(Symbols.NAMES)) {
        bounds.attributeCouldBePresent(Symbols.NAMES);
      }
    }
    bounds.closeAttributes();
  }


  private void buildAllBounds(ValueBounds.Builder bounds, List<ValueBounds> argumentBounds) {

    boolean closed = true;

    for (ValueBounds argumentBound : argumentBounds) {
      if(argumentBound.isAttributeSetOpen()) {
        closed = false;
      }

      for (Symbol attribute : argumentBound.getAttributeBounds().keySet()) {
        bounds.attributeCouldBePresent(attribute);
      }
    }

    if(closed) {
      bounds.closeAttributes();
    }
  }

  public Specialization specializeFurther() {
    if(ValueBounds.allConstant(argumentBounds)) {
      return evaluateConstant();
    }

    if(resultBounds.isFlagSet(ValueBounds.FLAG_LENGTH_ONE | ValueBounds.FLAG_NO_NA) &&
        resultBounds.hasNoAttributes()) {

      DoubleBinaryOp op = DoubleBinaryOp.trySpecialize(name, method, resultBounds);
      if(op != null) {
        return op;
      }
      return new DataParallelScalarCall(method, argumentBounds, resultBounds).trySpecializeFurther();
    }
    return this;
  }

  private Specialization evaluateConstant() {

    assert !method.acceptsArgumentList();

    List<JvmMethod.Argument> formals = method.getAllArguments();
    Object[] args = new Object[formals.size()];
    Iterator<ValueBounds> it = argumentBounds.iterator();
    int argIndex = 0;
    for (JvmMethod.Argument formal : formals) {
      if(formal.isContextual()) {
        throw new UnsupportedOperationException("in " + method +  ", " + "formal: " + formal);
      } else {
        ValueBounds argument = it.next();
        args[argIndex++] = ConstantCall.convert(argument.getConstantValue(), formal.getClazz());
      }
    }

    Object constantValue;
    try {
      constantValue = method.getMethod().invoke(null, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new ConstantCall(constantValue);
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
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {

        // Call into R$primitive$$plus$$doApply

        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

        for (IRArgument argument : arguments) {
          argument.getExpression().getCompiledExpr(emitContext).loadSexp(emitContext, mv);
        }

        mv.invokestatic(getWrapperInternalClassName(), "doApply",
            getWrapperApplySignature(arguments), false);
      }
    };
  }

  private String getWrapperInternalClassName() {
    return "org/renjin/primitives/" + WrapperGenerator2.toJavaName(name);
  }

  private String getWrapperApplySignature(List<IRArgument> arguments) {
    String sexpDescriptor = "L" + BytecodeTypes.SEXP_INTERNAL_NAME + ";";
    StringBuilder signature = new StringBuilder();
    signature.append('(');
    signature.append(Type.getDescriptor(Context.class));
    signature.append(Type.getDescriptor(Environment.class));
    for (int i = 0; i < arguments.size(); i++) {
      signature.append(sexpDescriptor);
    }
    signature.append(')');
    signature.append(sexpDescriptor);
    return signature.toString();
  }
}
