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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.IntegerComparison;
import org.renjin.gcc.codegen.condition.ObjectIsCondition;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleOp;

import static org.renjin.gcc.codegen.expr.Expressions.identityHash;

/**
 * Compares two JVM reference expressions.
 * 
 * <p>For EQ_EXPR and NE_EXPR, we use standard {@code IF_ACMPEQ} and {@code IF_ACMPNE} bytecode instructions.</p>
 * 
 * <p>For LT_EXPR and GT_EXPR, we compare the result of {@link System#identityHashCode(Object)}</p>
 * 
 */
public class ReferenceConditions {


  public static ConditionGenerator compare(GimpleOp op, JExpr x, JExpr y) {
    switch (op) {
      case EQ_EXPR:
        return new ObjectIsCondition(x, y);
      case NE_EXPR:
        return new ObjectIsCondition(x, y).inverse();
      default:
        return new IntegerComparison(op, identityHash(x), identityHash(y));
    }
  }
}
