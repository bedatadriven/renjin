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
import org.renjin.compiler.codegen.expr.ArrayExpr;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Summary;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.sexp.DoubleVector;

import java.util.ArrayList;
import java.util.List;

public class RangeSpecializer implements BuiltinSpecializer {

  private final JvmMethod fallback;

  public RangeSpecializer() {
    fallback = Iterables.getOnlyElement(JvmMethod.findOverloads(Summary.class, "range", "range"));
  }


  @Override
  public String getName() {
    return "range";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {

    ArgumentBounds naRm = null;
    List<ArgumentBounds> operands = new ArrayList<>();

    for (ArgumentBounds argument : arguments) {
      if(argument.isNamed() && "na.rm".equals(argument.getName())) {
        naRm = argument;
      } else {
        operands.add(argument);
      }
    }

    if(naRm == null && operands.size() == 1) {
      ArgumentBounds operand = operands.get(0);
      if(operand.getTypeSet() == TypeSet.DOUBLE) {
        return new DoubleRange(operand);
      }
    }
    return new StaticMethodCall(fallback, arguments);
  }


  private static class DoubleRange implements Specialization {

    private final ArgumentBounds argument;
    private final ValueBounds result;

    public DoubleRange(ArgumentBounds argument) {
      this.argument = argument;
      this.result = ValueBounds.builder()
          .setTypeSet(TypeSet.DOUBLE)
          .setLength(ValueBounds.LENGTH_NON_ZERO)
          .addFlagsFrom(argument.getBounds(), ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE)
          .build();
    }

    @Override
    public ValueBounds getResultBounds() {
      return result;
    }

    @Override
    public boolean isPure() {
      return true;
    }

    @Override
    public CompiledSexp getCompiledExpr(EmitContext emitContext) {
      return new ArrayExpr(VectorType.DOUBLE) {
        @Override
        public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
          argument.getCompiledExpr(emitContext).loadSexp(context, mv);
          mv.checkcast(Type.getType(DoubleVector.class));
          mv.invokestatic(Type.getInternalName(Summary.class), "range", "(Lorg/renjin/sexp/DoubleVector;)[D", false);
        }
      };
    }
  }
}
