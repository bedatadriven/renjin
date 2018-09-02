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

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.primitives.combine.Combine;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;

import java.util.List;

/**
 * Specializes calls to {@code c}
 */
public class CombineSpecializer implements Specializer, BuiltinSpecializer {


  @Override
  public String getName() {
    return "c";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {

    SEXP constantResult = tryCombine(arguments);
    if(constantResult != null) {
      return new ConstantCall(constantResult);
    }

    if(allArgumentsArePrimitives(arguments)) {
      return new CombinePrimitives(arguments);
    }

    return UnspecializedCall.PURE;
  }

  private SEXP tryCombine(List<ArgumentBounds> arguments) {
    ListVector.NamedBuilder constants = ListVector.newNamedBuilder();
    for (ArgumentBounds argument : arguments) {
      if(argument.getBounds().isConstant()) {
        constants.add(argument.getName(), argument.getBounds().getConstantValue());
      } else {
        return null;
      }
    }

    return Combine.c(constants.build(), false);

  }

  private boolean allArgumentsArePrimitives(List<ArgumentBounds> arguments) {
    for (ArgumentBounds argument : arguments) {
      if(argument.getName() != null) {
        return false;
      }
      if((argument.getBounds().getTypeSet() & ~TypeSet.ANY_ATOMIC_VECTOR) != 0) {
        return false;
      }
    }
    return true;
  }

  private boolean allAreConstant(List<ValueBounds> argumentBounds) {
    for (ValueBounds argumentBound : argumentBounds) {
      if(!argumentBound.isConstant()) {
        return false;
      }
    }
    return true;
  }

  private static class CombinePrimitives implements Specialization {

    private final ValueBounds bounds;

    public CombinePrimitives(List<ArgumentBounds> argumentBounds) {
      int length = 0;
      int typeSet = TypeSet.NULL;
      for (ArgumentBounds argumentBound : argumentBounds) {
        typeSet = Math.max(typeSet, argumentBound.getBounds().getTypeSet());
        length++;
      }
      bounds = ValueBounds.builder()
          .setLength(length)
          .setTypeSet(typeSet)
          .build();
    }

    @Override
    public ValueBounds getResultBounds() {
      return bounds;
    }

    @Override
    public boolean isPure() {
      return true;
    }

    @Override
    public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {
      throw new UnsupportedOperationException("TODO");
    }

  }


}
