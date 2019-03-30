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

public class DynamicEnvironment extends Environment {
  private final Frame frame;

  public DynamicEnvironment(String name, Environment parent, AttributeMap attributes, Frame frame) {
    super(parent, name, attributes);
    this.frame = frame;
  }

  public DynamicEnvironment(String name, Environment parent, Frame frame) {
    this(name, parent, AttributeMap.EMPTY, frame);
  }

  public DynamicEnvironment(Environment parent) {
    this(null, parent, AttributeMap.EMPTY, new HashFrame());
  }

  public DynamicEnvironment(Environment parent, Iterable<NamedValue> namedValues) {
    this(null, parent, AttributeMap.EMPTY, new HashFrame(namedValues));
  }

  public DynamicEnvironment(AttributeMap attributes) {
    this(null, Environment.EMPTY, attributes, new HashFrame());
  }

  @Override
  protected Collection<Symbol> listBindings() {
    return frame.getSymbols();
  }

  @Override
  protected boolean isBound(Symbol symbol) {
    return frame.getVariable(symbol) != Symbol.UNBOUND_VALUE;
  }

  @Override
  protected SEXP getBinding(Symbol symbol) {
    return frame.getVariable(symbol);
  }

  @Override
  protected Function getFunctionBinding(Context context, Symbol symbol) {
    return frame.getFunction(context, symbol);
  }

  @Override
  protected void removeBinding(Symbol symbol) {
    frame.remove(symbol);
  }

  @Override
  protected void updateBinding(Symbol symbol, SEXP value) {
    frame.setVariable(symbol, value);
  }
}
