/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.sexp;

import org.renjin.eval.Context;

import java.util.HashMap;
import java.util.Set;

public class LocalFrame implements Frame {

  private SEXP[] locals;
  private Symbol[] localNames;
  private HashMap<Symbol, SEXP> overflow = null;


  public LocalFrame(SEXP[] locals) {
    this.locals = locals;
  }

  public SEXP get(int localIndex) {
    throw new UnsupportedOperationException("TODO");
  }

  public void set(int localIndex, SEXP value) {
    locals[localIndex] = value;
  }

  @Override
  public Set<Symbol> getSymbols() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SEXP getVariable(Symbol name) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Function getFunction(Context context, Symbol name) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean isMissingArgument(Symbol name) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void remove(Symbol name) {
    throw new UnsupportedOperationException("TODO");
  }

  public Promise promise(int localVarIndex) {
    return null;
  }
}
