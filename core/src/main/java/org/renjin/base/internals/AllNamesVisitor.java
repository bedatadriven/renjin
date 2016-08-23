package org.renjin.base.internals;

import org.renjin.invoke.annotations.Internal;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.*;

import java.util.Set;

public class AllNamesVisitor extends SexpVisitor<StringVector> {

  private StringVector.Builder names = StringVector.newBuilder();
  private Set<Symbol> set = Sets.newIdentityHashSet();
  private boolean includeFunctionNames;
  private int maxNames;
  private boolean unique;
  
  @Override
  public void visit(ExpressionVector vector) {
    for(SEXP expr : vector) {
      expr.accept(this);
    }
  }

  @Override
  public void visit(FunctionCall call) {
    if(includeFunctionNames) {
      call.getFunction().accept(this);
    }
    for(SEXP expr : call.getArguments().values()) {
      expr.accept(this);
    }
  }

  @Override
  public void visit(Symbol name) {
    if(!unique || !set.contains(name)) {
      if(maxNames == -1 || names.length() < maxNames) {
        names.add(StringArrayVector.valueOf(name.getPrintName()));
        set.add(name);
      }
    }
  }
  
  @Internal("all.names")
  public static StringVector allNames(SEXP expr, boolean function, int maxNames, boolean unique) {
    AllNamesVisitor visitor = new AllNamesVisitor();
    visitor.includeFunctionNames = function;
    visitor.maxNames = maxNames;
    visitor.unique = unique;
    expr.accept(visitor);
    return visitor.names.build();
  }
}
