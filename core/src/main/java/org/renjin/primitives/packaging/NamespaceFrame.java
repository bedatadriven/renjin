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
