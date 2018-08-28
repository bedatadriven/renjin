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

package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.SexpTypes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

class SexpElementAt implements CompiledSexp {
  private final CompiledSexp sexpExpr;
  private final CompiledSexp indexExpr;

  public SexpElementAt(CompiledSexp sexpExpr, CompiledSexp indexExpr) {
    this.sexpExpr = sexpExpr;
    this.indexExpr = indexExpr;
  }

  @Override
  public void loadSexp(EmitContext context, InstructionAdapter mv) {
    sexpExpr.loadSexp(context, mv);
    indexExpr.loadScalar(context, mv, VectorType.INT);
    mv.invokeinterface(SexpTypes.SEXP_INTERNAL_NAME, "getElementAsSEXP", "(I)L" + SexpTypes.SEXP_INTERNAL_NAME + ";");
  }

  @Override
  public void loadScalar(EmitContext context, InstructionAdapter mv, VectorType type) {
    sexpExpr.loadSexp(context, mv);
    indexExpr.loadScalar(context, mv, VectorType.INT);

    switch (type) {
      case BYTE:
        mv.invokeinterface(SexpTypes.SEXP_INTERNAL_NAME, "getElementAsByte", "(I)B");
        break;
      case LOGICAL:
      case INT:
        mv.invokeinterface(SexpTypes.SEXP_INTERNAL_NAME, "getElementAsInt", "(I)I");
        break;
      case DOUBLE:
        mv.invokeinterface(SexpTypes.SEXP_INTERNAL_NAME, "getElementAsDouble", "(I)D");
        break;
      case STRING:
        mv.invokeinterface(SexpTypes.SEXP_INTERNAL_NAME, "getElementAsString", "(I)Ljava/lang/String;");
        break;
      default:
        throw new UnsupportedOperationException(type.toString());
    }
  }

  @Override
  public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void loadLength(EmitContext context, InstructionAdapter mv) {
    loadSexp(context, mv);
    mv.invokeinterface(SexpTypes.SEXP_INTERNAL_NAME, "length", "()I");
  }

  @Override
  public CompiledSexp elementAt(EmitContext context, CompiledSexp indexExpr) {
    return new SexpElementAt(this, indexExpr);
  }
}
