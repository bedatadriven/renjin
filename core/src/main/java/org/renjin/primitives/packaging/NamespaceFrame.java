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
package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.ImmutableSet;
import org.renjin.sexp.Frame;
import org.renjin.sexp.Function;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Set;


public class NamespaceFrame implements Frame {

  private final NamespaceRegistry registry;

  public NamespaceFrame(NamespaceRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Set<Symbol> getSymbols() {
    return ImmutableSet.copyOf(registry.getLoadedNamespaces());
  }

  @Override
  public SEXP getVariable(Symbol name) {
    Optional<Namespace> namespace = registry.getNamespaceIfPresent(name);
    if(namespace.isPresent()) {
      return namespace.get().getNamespaceEnvironment();
    } else {
      return Symbol.UNBOUND_VALUE;
    }
  }

  @Override
  public Function getFunction(Context context, Symbol name) {
    return null;
  }

  @Override
  public boolean isMissingArgument(Symbol name) {
    return false;
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    throw new EvalException("Cannot modify the namespace registry");
  }

  @Override
  public void clear() {
    throw new EvalException("Cannot modify the namespace registry");
  }

  @Override
  public void remove(Symbol name) {
    throw new EvalException("Cannot modify the namespace registry");
  }
}
