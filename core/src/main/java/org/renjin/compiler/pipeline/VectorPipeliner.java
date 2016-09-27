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
package org.renjin.compiler.pipeline;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.Vector;

public interface VectorPipeliner {

  public static boolean DEBUG = "true".equals(System.getProperty("renjin.vp.debug"));
  public static int MAX_DEPTH = 25;

  /**
   * Calculate the result of the deferred computation and store in memory.
   * @param root the root of the calculation
   * @return
   */
  Vector materialize(DeferredComputation root);


  /**
   * Conducts any necessary simplification of the deferred computation graph
   * to keep the graph from growing too large
   */
  Vector simplify(DeferredComputation sexp);
}
