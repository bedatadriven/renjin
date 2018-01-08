/*
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
package org.renjin.eval;

import org.renjin.sexp.SEXP;

/**
 * Exception to pass control out of a block where a condition was signaled
 */
public class ConditionException extends RuntimeException {

  private SEXP condition;
  private Context handlerContext;
  private SEXP handler;

  public ConditionException(SEXP condition, Context handlerContext, SEXP handler) {
    this.condition = condition;
    this.handlerContext = handlerContext;
    this.handler = handler;
  }

  public SEXP getCondition() {
    return condition;
  }

  public Context getHandlerContext() {
    return handlerContext;
  }

  public SEXP getHandler() {
    return handler;
  }
}
