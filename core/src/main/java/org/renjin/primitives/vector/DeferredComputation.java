/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.vector;

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Vector;

/**
 * Marker interface for vectors whose computation has been deferred.
 * Deferred computations must be entirely pure: there can be no side
 * effects to their computation, and they must produce the same result
 * no matter how many times they are invoked.
 *
 * <p>
 *   Futhermore, a DeferredComputation of a given {@code class} must
 *   produce the same result given equal operands. That is, all details
 *   of the computation must be exposed as operands. If attributes play
 *   a role in computations, those attributes must be explicitly exposed
 *   as operands.
 * </p>
 */
public interface DeferredComputation extends AtomicVector {

  /**
   * @return this
   */
  Vector[] getOperands();

  /**
   * @return the name of the computation ("sum", "*", "/", etc), used
   * mostly for debugging.
   */
  String getComputationName();


}
