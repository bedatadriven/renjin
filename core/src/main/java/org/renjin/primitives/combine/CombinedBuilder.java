/**
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
package org.renjin.primitives.combine;


import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * Common interface to combined vector builders, for which
 * we have different implementations depending on the shape of the data
 */
interface CombinedBuilder {

  /**
   * Enables or disables copying of names to the output vector
   */
  CombinedBuilder useNames(boolean useNames);

  /**
   * Adds an S Expression to the output vector
   * @param prefix the prefix for the name if this object comes from a list that was named.
   * @param sexp an R object to add to the list
   */
  void add(String prefix, SEXP sexp);

  /**
   * Adds all of the elments in the vector {@code vectorElement} to the output vector
   *
   * @param prefix
   * @param vectorElement
   */
  void addElements(String prefix, Vector vectorElement);

  /**
   *
   * @return the vector that combines all of the input elements
   */
  Vector build();

}
