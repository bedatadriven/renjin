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
import org.renjin.repackaged.asm.commons.InstructionAdapter;

class ArrayElementAt extends ScalarExpr {

  private final ArrayExpr arrayExpr;
  private final CompiledSexp indexExpr;

  public ArrayElementAt(ArrayExpr arrayExpr, CompiledSexp indexExpr) {
    super(arrayExpr.getVectorType());
    this.arrayExpr = arrayExpr;
    this.indexExpr = indexExpr;
  }

  @Override
  public void loadScalar(EmitContext context, InstructionAdapter mv) {
    arrayExpr.loadArray(context, mv, arrayExpr.getVectorType());
    indexExpr.loadScalar(context, mv, VectorType.INT);
    arrayExpr.loadElement(mv);


  }
}
