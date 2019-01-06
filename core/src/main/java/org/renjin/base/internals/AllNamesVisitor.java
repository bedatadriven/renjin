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
