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
package org.renjin.compiler;


import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

public interface CompiledLoopBody {

  /**
   * The index of the local variable holding the evaluation context variable.
   */
  int CONTEXT_PARAM_INDEX = 1;

  /**
   * The index of the local variable holding the evaluation environment variable.
   */
  int ENV_PARAM_INDEX = 2;


  /**
   * The index of the local variable hodling the loop elements vector.
   */
  int LOOP_VECTOR_INDEX = 3;

  /**
   * The index of the local variable holding the loop iteration index.
   */
  int LOOP_ITERATION_INDEX = 4;


  /**
   * The number of local variable used by this and the arguments.
   * (this + context + environment + sequence + iteration)
   */
  int PARAM_SIZE = 5;



  SEXP run(Context context, Environment rho, SEXP sequence, int iteration);
  
}
