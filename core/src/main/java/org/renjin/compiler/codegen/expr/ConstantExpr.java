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
package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.repackaged.asm.ByteVector;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

public class ConstantExpr {

  private ConstantExpr() {
  }

  public static CompiledSexp createConstantExpr(SEXP sexp) {

    if((sexp instanceof DoubleVector ||
        sexp instanceof IntVector ||
        sexp instanceof ByteVector ||
        sexp instanceof StringVector) && sexp.length() == 1 && sexp.getAttributes().isEmpty()) {

      VectorType vectorType = VectorType.of(TypeSet.of(sexp));
      return new ScalarExpr(vectorType) {
        @Override
        public void loadScalar(EmitContext context, InstructionAdapter mv) {
          switch (vectorType) {
            case BYTE:
              mv.iconst(((Vector) sexp).getElementAsByte(0));
              break;
            case INT:
              mv.iconst(((Vector) sexp).getElementAsInt(0));
              break;
            case DOUBLE:
              mv.dconst(((Vector) sexp).getElementAsDouble(0));
              break;
            case STRING:
              mv.aconst(((Vector) sexp).getElementAsString(0));
              break;
          }
        }
      };
    } else {
      return new SexpExpr() {
        @Override
        public void loadSexp(EmitContext context, InstructionAdapter mv) {
          context.constantSexp(sexp).loadSexp(context, mv);
        }
      };
    }
  }
}
