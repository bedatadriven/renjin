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
package org.renjin.eval;

import org.renjin.primitives.special.ControlFlowException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Function;
import org.renjin.sexp.ListVector;

/**
 * Raised to return control flow to the context in which a restart was defined.
 *
 */
public class RestartException extends ControlFlowException {
  private Environment exitEnvironment;
  private Function handler;
  private ListVector arguments;

  public RestartException(Environment exitEnvironment, Function handler, ListVector arguments) {
    this.exitEnvironment = exitEnvironment;
    this.handler = handler;
    this.arguments = arguments;
  }

  public Environment getExitEnvironment() {
    return exitEnvironment;
  }

  public Function getHandler() {
    return handler;
  }

  public ListVector getArguments() {
    return arguments;
  }
}
