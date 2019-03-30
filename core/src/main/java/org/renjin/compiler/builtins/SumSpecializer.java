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

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;

import java.util.List;

public class SumSpecializer implements BuiltinSpecializer {

  private final JvmMethod fallback;

  public SumSpecializer() {
    fallback = AnnotationBasedSpecializer.findMethod("sum");
  }

  @Override
  public String getName() {
    return "sum";
  }

  @Override
  public String getGroup() {
    return "Summary";
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {

    int resultType = TypeSet.INT;
    int summandCount = 0;
    boolean hasNoNAs = true;
    boolean naRm = false;

    for (ArgumentBounds argument : arguments) {
      if("na.rm".equals(argument.getName())) {
        if(argument.getBounds().isConstantFlagEqualTo(true)) {
          naRm = true;
        }
      } else {
        summandCount ++;

        ValueBounds bounds = argument.getBounds();
        int argumentTypeSet = bounds.getTypeSet();

        // Promote logical vectors to integers
        if((argumentTypeSet & TypeSet.LOGICAL) != 0) {
          argumentTypeSet &= ~TypeSet.LOGICAL;
          argumentTypeSet |= TypeSet.INT;
        }

        resultType = TypeSet.widestVectorType(resultType,
            (argumentTypeSet & (TypeSet.INT | TypeSet.DOUBLE | TypeSet.COMPLEX)));

        if(!bounds.isFlagSet(ValueBounds.FLAG_NO_NA)) {
          hasNoNAs = false;
        }
      }
    }

    boolean naResultPossible = hasNoNAs || naRm;

    // Integers can overflow to NA
    if(TypeSet.mightBe(resultType, TypeSet.INT)) {
      naResultPossible = true;
    }

    ValueBounds resultBounds = ValueBounds.builder()
        .setTypeSet(resultType)
        .addFlags(ValueBounds.LENGTH_ONE)
        .addFlags(ValueBounds.FLAG_NO_NA, naResultPossible)
        .build();

    if(summandCount == 1 && arguments.size() == 1 && resultType == TypeSet.DOUBLE) {
      return new SumSpecialization(resultBounds);
    }

    return new StaticMethodCall(fallback, resultBounds);
  }
}
