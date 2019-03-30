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
package org.renjin.compiler.codegen;

import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;

public final class BytecodeTypes {

  public static final Type SEXP_TYPE = Type.getType(SEXP.class);

  public static final Type ATOMIC_VECTOR_TYPE = Type.getType(AtomicVector.class);

  public static final String SEXP_INTERNAL_NAME = SEXP_TYPE.getInternalName();

  public static final String ATOMIC_VECTOR_INTERNAL_NAME = ATOMIC_VECTOR_TYPE.getInternalName();

  private BytecodeTypes() {
  }
}
