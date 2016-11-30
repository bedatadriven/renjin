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
package org.renjin.eval;

import org.renjin.sexp.*;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Maintains finalization methods registered by users
 * for {@code Environment}s. Finalizers will only be invoked
 * when contexts are cleaned up.
 */
public class FinalizerRegistry {

  private class Finalizer extends WeakReference<SEXP> {
    private final FinalizationHandler function;
    private boolean onExit;

    public Finalizer(SEXP sexp, FinalizationHandler function, boolean onExit) {
      super(sexp, queue);
      this.function = function;
      this.onExit = onExit;
    }

    private void invoke(Context context) {
      function.finalize(context, get());
    }

    public SEXP getSexp() {
      SEXP e = get();
      if(e == null) {
        throw new IllegalStateException("Environment has already been garbage collected!");
      }
      return e;
    }

    public boolean isOnExit() {
      return onExit;
    }
  }

  private final ReferenceQueue<SEXP> queue = new ReferenceQueue<SEXP>();
  private final Set<Finalizer> finalizers = new HashSet<Finalizer>();

  public void register(SEXP sexp, FinalizationHandler handler, boolean onExit) {
    finalizers.add(new Finalizer(sexp, handler, onExit));
  }

  /**
   * Finalizes all environments that have queued for finalization by the garbage
   * collector.
   *
   * @param context the context in which to evaluate the finalizer function.
   */
  public void finalizeDisposedEnvironments(Context context) {
    Reference<? extends SEXP> ref;
    while((ref = queue.poll()) != null) {
      Finalizer finalizer = (Finalizer)ref;
      finalizers.remove(finalizer);

      finalizer.invoke(context);
    }
  }

  public void finalizeOnExit(Context context) {
    List<Finalizer> toInvoke = new ArrayList<Finalizer>(this.finalizers);
    this.finalizers.clear();

    for(Finalizer finalizer : toInvoke) {
      if(finalizer.isOnExit()) {
        finalizer.invoke(context);
      }
    }
  }
}
