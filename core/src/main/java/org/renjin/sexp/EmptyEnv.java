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
package org.renjin.sexp;

import org.renjin.eval.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

class EmptyEnv extends Environment {

  EmptyEnv() {
    super(null, null, AttributeMap.EMPTY);
  }

  @Override
  public SEXP findVariable(Context context, Symbol symbol, Predicate<SEXP> predicate, boolean inherits) {
    return Symbol.UNBOUND_VALUE;
  }

  @Override
  public SEXP findVariable(Context context, Symbol symbol) {
    return Symbol.UNBOUND_VALUE;
  }

  @Override
  protected Collection<Symbol> listBindings() {
    return Collections.emptyList();
  }

  @Override
  protected boolean isBound(Symbol symbol) {
    return false;
  }

  @Override
  protected SEXP getBinding(Symbol symbol) {
    return Symbol.UNBOUND_VALUE;
  }

  @Override
  protected Function getFunctionBinding(Context context, Symbol symbol) {
    return null;
  }

  @Override
  protected void removeBinding(Symbol symbol) {
  }

  @Override
  protected void updateBinding(Symbol symbol, SEXP value) {
  }

  @Override
  public Function findFunction(Context context, Symbol symbol) {
    return null;
  }
}
