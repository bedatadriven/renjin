package org.renjin.eval;

import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;

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

  private class Finalizer extends WeakReference<Environment> {
    private final Closure function;
    private boolean onExit;

    public Finalizer(Environment environment, Closure function, boolean onExit) {
      super(environment, queue);
      this.function = function;
      this.onExit = onExit;
    }

    private void invoke(Context context) {
      context.evaluate(new FunctionCall(function, PairList.Node.singleton(getEnvironment())));
    }

    public Environment getEnvironment() {
      Environment e = get();
      if(e == null) {
        throw new IllegalStateException("Environment has already been garbage collected!");
      }
      return e;
    }

    public boolean isOnExit() {
      return onExit;
    }
  }

  private final ReferenceQueue<Environment> queue = new ReferenceQueue<Environment>();
  private final Set<Finalizer> finalizers = new HashSet<Finalizer>();

  public void register(Environment environment, Closure closure, boolean onExit) {
    finalizers.add(new Finalizer(environment, closure, onExit));
  }

  /**
   * Finalizes all environments that have queued for finalization by the garbage
   * collector.
   *
   * @param context the context in which to evaluate the finalizer function.
   */
  public void finalizeDisposedEnvironments(Context context) {
    Reference<? extends Environment> ref;
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
