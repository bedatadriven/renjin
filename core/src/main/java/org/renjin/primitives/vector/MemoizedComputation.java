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

import org.renjin.sexp.Vector;

/**
 * Marker interface for DeferredComputation whose value is <em>memoized</em>,
 * that is, it is not computed until it's result is used, but once computed
 * its result is stored internally to avoid being computed again.
 *
 */
public interface MemoizedComputation extends DeferredComputation {

  /**
   *
   * @return ensure that the result has been calculated and return it.
   */
  Vector forceResult();

  /**
   * Sets the results of the computation. This may be called in the event
   *  that the computation is done externally.
   */
  void setResult(Vector result);

  /**
   *
   * @return  true if the result has been calculated and is available in memory.
   */
  boolean isCalculated();

}
