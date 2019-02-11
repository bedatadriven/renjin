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
/**
 * Code generation for complex numbers, with either double or single precision.
 * 
 * <p>Fortran, and now C99, provide intrinsic complex types. Supporting complex numbers
 * as value types requires different strategies depending on the context. For local
 * variables, for example, we just use two local variables for the real and imaginary parts.
 * When returning a complex value however, we have no choice but to allocate an array to store the two
 * components and then return the reference to the array.</p>
 */
package org.renjin.gcc.codegen.type.complex;