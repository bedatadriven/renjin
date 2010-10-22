/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Iterators.filter;
import static r.lang.ListExp.Predicates;

public class ClosureExp extends SEXP implements FunSxp {

  private EnvExp enclosingEnvironment;
  private SEXP statement;
  private ListExp formals;

  public ClosureExp(EnvExp enclosingEnvironment, ListExp formals, SEXP statement) {
    this.enclosingEnvironment = enclosingEnvironment;
    this.statement = statement;
    this.formals = formals;
  }

  @Override
  public Type getType() {
    return Type.CLOSXP;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public SEXP apply(LangExp call, ListExp args, EnvExp rho) {

    EnvExp env = new EnvExp(enclosingEnvironment);

    Iterator<ListExp> formalIt = formals.listNodes().iterator();
    Iterator<SEXP> actualIt = ListExp.iterator(args);

    while(formalIt.hasNext()) {
      SymbolExp formal = (SymbolExp)formalIt.next().getTag();
      if(actualIt.hasNext()) {
        env.setVariable(formal, actualIt.next());
      } else {
        env.setVariable(formal, SymbolExp.MISSING_ARG);
      }
    }

    return statement.evaluate(env);
  }

  /**
   * This is done by a three-pass process:
   * <ol>
   * <li><strong>Exact matching on tags.</strong> For each named supplied argument the list of formal arguments
   *  is searched for an item whose name matches exactly. It is an error to have the same formal
   * argument match several actuals or vice versa.</li>
   *
   * <li><strong>Partial matching on tags.</strong> Each remaining named supplied argument is compared to the
   * remaining formal arguments using partial matching. If the name of the supplied argument
   * matches exactly with the first part of a formal argument then the two arguments are considered
   * to be matched. It is an error to have multiple partial matches.
   *  Notice that if f <- function(fumble, fooey) fbody, then f(f = 1, fo = 2) is illegal,
   * even though the 2nd actual argument only matches fooey. f(f = 1, fooey = 2) is legal
   * though since the second argument matches exactly and is removed from consideration for
   * partial matching. If the formal arguments contain ‘...’ then partial matching is only applied to
   * arguments that precede it.
   *
   * <li><strong>Positional matching.</strong> Any unmatched formal arguments are bound to unnamed supplied arguments,
   * in order. If there is a ‘...’ argument, it will take up the remaining arguments, tagged or not.
   * If any arguments remain unmatched an error is declared.
   *
   * @param actuals the actual arguments supplied to the list
   * @param env the environment in which to resolve the arguments
   */
  private void matchArguments(ListExp actuals, EnvExp env) {

    List<ListExp> unmatchedActuals = Lists.newArrayList(actuals.listNodes());
    List<ListExp> unmatchedFormals = Lists.newArrayList(formals.listNodes());

    for(Iterator<ListExp> actualIt = filter(unmatchedActuals.iterator(), Predicates.hasTag());
        actualIt.hasNext(); ) {




    }
  }
  

}
