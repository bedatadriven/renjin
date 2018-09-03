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

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * Specialization for builtins that are marked {@link org.renjin.invoke.annotations.DataParallel} and
 * whose arguments are "recycled" for multiple calls.
 */
public class DataParallelCall {

  private final Primitives.Entry primitive;
  private final JvmMethod method;
  private final List<ArgumentBounds> argumentBounds;
  private final ValueBounds resultBounds;

  public DataParallelCall(Primitives.Entry primitive, JvmMethod method, List<ArgumentBounds> argumentBounds) {
    this.primitive = primitive;
    this.method = method;
    this.argumentBounds = argumentBounds;
    this.resultBounds = computeBounds(argumentBounds);
  }

  private ValueBounds computeBounds(List<ArgumentBounds> argumentBounds) {

    List<ValueBounds> recycledArguments = recycledArgumentBounds(argumentBounds);

    ValueBounds.Builder bounds = new ValueBounds.Builder();
    bounds.setType(method.getReturnType());
    bounds.addFlags(computeFlags(argumentBounds,
        ValueBounds.FLAG_NO_NA | ValueBounds.LENGTH_NON_ZERO | ValueBounds.LENGTH_ONE));

    switch (method.getPreserveAttributesStyle()) {
      case NONE:
        // Result will have no attributes
        break;
      case STRUCTURAL:
        // Result may only have "dim", "names" or "dimnames" attributes
        bounds.addFlags(computeAttributes(recycledArguments,
            ValueBounds.MAYBE_DIM | ValueBounds.MAYBE_DIMNAMES | ValueBounds.MAYBE_NAMES));
        break;
      case ALL:
        // Result may have any of the attributes present in the arguments
        bounds.addFlags(computeAttributes(recycledArguments, ValueBounds.MAYBE_ATTRIBUTES));
        break;
    }

    return bounds.build();
  }

  private int computeFlags(List<ArgumentBounds> argumentBounds, int mask) {
    assert !argumentBounds.isEmpty();

    // These properties are preserved if all arguments share them
    int flags = mask;

    for (ArgumentBounds argumentBound : argumentBounds) {
      flags &= argumentBound.getFlags();
    }

    return flags;
  }

  private int computeAttributes(List<ValueBounds> argumentBounds, int mask) {
    int flags = 0;

    for (ValueBounds argumentBound : argumentBounds) {
      flags |= (argumentBound.getFlags() & mask);
    }

    return flags;
  }

  /**
   * Makes a list of {@link ValueBounds} for @Recycled arguments.
   */
  private List<ValueBounds> recycledArgumentBounds(List<ArgumentBounds> argumentBounds) {
    List<ValueBounds> list = Lists.newArrayList();
    Iterator<ArgumentBounds> argumentIt = argumentBounds.iterator();
    for (JvmMethod.Argument formal : method.getFormals()) {
      if (formal.isRecycle()) {
        list.add(argumentIt.next().getBounds());
      }
    }
    return list;
  }


  public Specialization specialize() {
    if(ValueBounds.allConstantArguments(argumentBounds)) {
      return evaluateConstant();
    }

    if(resultBounds.isFlagSet(ValueBounds.LENGTH_ONE | ValueBounds.FLAG_NO_NA) &&
        resultBounds.hasNoAttributes()) {

      DoubleBinaryOp op = DoubleBinaryOp.trySpecialize(primitive.name, method, argumentBounds, resultBounds);
      if (op != null) {
        return op;
      }
      return new DataParallelScalarCall(method, argumentBounds, resultBounds).trySpecializeFurther();

    } else if(
        resultBounds.hasNoAttributes() &&
        method.getPositionalFormals().size() == 1) {

      return new DataParallelUnaryOp(method, argumentBounds, resultBounds);

    } else if(resultBounds.hasNoAttributes() &&
              method.getReturnType().equals(double.class) &&
              method.getPositionalFormals().size() == 2 &&
              method.getPositionalFormals().get(0).getClazz().equals(double.class) &&
              method.getPositionalFormals().get(1).getClazz().equals(double.class)) {

      return new DoubleBinaryArrayOp(method, argumentBounds, resultBounds);
    }
    return new WrapperApplyCall(primitive, argumentBounds, resultBounds);
  }

  private Specialization evaluateConstant() {

    assert !method.acceptsArgumentList();

    List<JvmMethod.Argument> formals = method.getAllArguments();
    Object[] args = new Object[formals.size()];
    Iterator<ArgumentBounds> it = argumentBounds.iterator();
    int argIndex = 0;
    for (JvmMethod.Argument formal : formals) {
      if(formal.isContextual()) {
        throw new UnsupportedOperationException("in " + method +  ", " + "formal: " + formal);
      } else {
        ArgumentBounds argument = it.next();
        args[argIndex++] = ConstantCall.convert(argument.getBounds().getConstantValue(), formal.getClazz());
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

}
