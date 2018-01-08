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
package org.renjin.embed;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;
import org.rosuda.JRI.Rengine;

import java.util.Set;

/**
 * A "magical" frame that searches for symbols in the GNU R host environment.
 *
 * <p>This frame implementation maintains its own copy of the host's environment tree.
 * <p>Note that we cannot just call {@link Rengine#rniFindVar(String, long)} to obtain the
 * symbol because we want to ensure that if the lookup reaches the stats namespace, we want to
 * do the lookup in Renjin's version of the stats namespace.
 */
public class HostFrame implements Frame {

  private final Rengine rengine;
  private final Wrapper wrapper;
  private final HostEnv hostEnv;

  private class HostEnv {
    private final HostEnv parent;
    private final Environment guest;

    public HostEnv(long envPtr) {
      // Obtain or create a wrapper for the host environment.
      // This will *either* be a wrapper for the host environment, OR
      // will be Renjin's own version of the environment, for example, the stats
      // namespace or the base environment.

      this.guest = (Environment)wrapper.wrap(envPtr);
      long parentEnvPtr = rengine.rniParentEnv(envPtr);
      if(wrapper.isEmptyEnv(parentEnvPtr)) {
        this.parent = null;
      } else {
        this.parent = new HostEnv(parentEnvPtr);
      }
    }
  }

  public HostFrame(Rengine rengine, Wrapper wrapper, long hostEnv) {
    this.rengine = rengine;
    this.wrapper = wrapper;
    this.hostEnv = new HostEnv(hostEnv);
  }

  @Override
  public Set<Symbol> getSymbols() {
    throw new EvalException("TODO: Listing symbols in the host frame?");
  }

  @Override
  public SEXP getVariable(Symbol name) {
    HostEnv env = hostEnv;
    while(env != null) {
      SEXP value = env.guest.getVariableUnsafe(name);
      if(value != Symbol.UNBOUND_VALUE) {
        return value;
      }
      env = env.parent;
    }
    return Symbol.UNBOUND_VALUE;
  }

  @Override
  public Function getFunction(Context context, Symbol name) {
    HostEnv env = hostEnv;
    while(env != null) {
      SEXP value = env.guest.getVariableUnsafe(name);
      if(value instanceof Promise) {
        value = value.force(context);
      }
      if(value instanceof Function) {
        return (Function) value;
      }
      env = env.parent;
    }
    return null;
  }

  @Override
  public boolean isMissingArgument(Symbol name) {
    return false;
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    throw new EvalException("The host environment is read-only.");
  }

  @Override
  public void clear() {
    throw new EvalException("The host environment is read-only.");
  }

  @Override
  public void remove(Symbol name) {
    throw new EvalException("The host environment is read-only.");
  }
}
