/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.List;

public class SumSpecializer implements BuiltinSpecializer {

  private final String name;

  public SumSpecializer(String name) {
    this.name = name;
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

    int resultType = 0;
    boolean hasNoNAs = true;
    boolean naRm = false;

    for (ArgumentBounds argument : arguments) {
      if("na.rm".equals(argument.getName())) {
        if(argument.getBounds().isConstantFlagEqualTo(true)) {
          naRm = true;
        }
      } else {
        ValueBounds bounds = argument.getBounds();
        if(resultType == 0) {
          resultType = bounds.getTypeSet();
        } else {
          resultType = TypeSet.widestVectorType(resultType, bounds.getTypeSet());
        }
        if(bounds.getNA() == ValueBounds.MAY_HAVE_NA) {
          hasNoNAs = false;
        }
      }
    }

    // Integer sums are promoted to double on overflow...
    if(TypeSet.mightBe(resultType, TypeSet.INT)) {
      resultType |= TypeSet.DOUBLE;
    }

    ValueBounds resultBounds = ValueBounds.builder()
        .setTypeSet(resultType)
        .setEmptyAttributes()
        .setLength(1)
        .setHasNoNAs(hasNoNAs || naRm)
        .build();

    return new SumSpecialization(resultBounds);
  }
}
