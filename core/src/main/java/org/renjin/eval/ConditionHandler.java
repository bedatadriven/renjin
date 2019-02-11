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

import org.renjin.sexp.SEXP;

/**
 * A handler registration for a condition.
 */
public class ConditionHandler {

  /**
   * The function to invoke if a signal is handled
   */
  private SEXP handler;


  /**
   * True if this is a calling handler. Calling handlers are executed in the context
   * in which the condition is signaled. If {@code false}, the control is first returned
   * to the {@link Context} in which the handler was registered.
   */
  private boolean calling;

  public ConditionHandler(SEXP handler, boolean calling) {
    this.handler = handler;
    this.calling = calling;
  }

  public SEXP getFunction() {
    return handler;
  }

  public boolean isCalling() {
    return calling;
  }
}
