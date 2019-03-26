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
 * Implementation of builtin subset operators {@code [}, {@code [[}, {@code $} and {@code @}
 * 
 * <p>These operators have complex behavior and are implemented in Renjin as follows:</p>
 * 
 * <ul>
 *   <li>The class {@link org.renjin.primitives.subset.Subsetting} provide the entry point for the builtins and
 *   perform very basic argument matching.</li>
 *   <li>These methods call {@link org.renjin.primitives.subset.Selections#parseSelection(org.renjin.sexp.SEXP, java.util.List)}
 *   to parse the provided arguments into a class provided subscripts are parsed into
 *   {@link org.renjin.primitives.subset.SelectionStrategy} objects depending on the arguments and their context.</li>
 * </ul>
 */
package org.renjin.primitives.subset;