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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import r.lang.exception.EvalException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static r.lang.PairListExp.Predicates;

/**
 * The function closure data type.
 *
 * <p>
 * In R functions are objects and can be manipulated in much the same way as any other object.
 * Functions (or more precisely, function closures) have three basic components:
 *  a formal argument list, a body and an environment.
 *
 */
public class ClosureExp extends SEXP implements FunExp {

  private static final String TYPE_NAME = "closure";
  private static final int TYPE_CODE = 4;

  private EnvExp enclosingEnvironment;
  private SEXP body;
  private PairList formals;

  public ClosureExp(EnvExp environment, PairList formals, SEXP body) {
    this.enclosingEnvironment = environment;
    this.body = body;
    this.formals = formals;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public EvalResult apply(LangExp call, PairList args, EnvExp rho) {

    EnvExp functionEnvironment = new EnvExp(enclosingEnvironment);
    matchArgumentsInto(args, functionEnvironment);

    return body.evaluate(functionEnvironment);
  }

  /**
   * A function's <strong> evaluation environment</strong> is the environment
   * that was active at the time that the
   * function was created. Any symbols bound in that environment are
   * captured and available to the function. This combination of the code of the
   * function and the bindings in its environment is called a `function closure', a
   * term from functional programming theory.
   *
   */
  public EnvExp getEnclosingEnvironment() {
    return enclosingEnvironment;
  }

  /**
   * The body is a parsed R statement.
   * It is usually a collection of statements in braces but it
   * can be a single statement, a symbol or even a constant.
   */
  public SEXP getBody() {
    return body;
  }

  /**
   * The formal argument list is a a pair list of arguments.
   * An argument can be a symbol, or a ‘symbol = default’ construct, or
   * the special argument ‘...’.
   *
   * <p> The second form of argument is
   *  used to specify a default value for an argument.
   * This value will be used if the function is called
   *  without any value specified for that argument.
   * The ‘...’ argument is special and can contain any number of arguments.
   * It is generally used if the number of arguments
   * is unknown or in cases where the arguments will
   * be passed on to another function.
   */
  public PairList getFormals() {
    return formals;
  }

  /**
   * Argument matching is done by a three-pass process:
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
   * @param innerEnv the environment in which to resolve the arguments
   */
  private void matchArgumentsInto(PairList actuals, EnvExp innerEnv) {

    List<PairListExp> unmatchedActuals = Lists.newArrayList(actuals.listNodes());
    List<PairListExp> unmatchedFormals = Lists.newArrayList(formals.listNodes());

    // do exact matching
    for(ListIterator<PairListExp> formalIt = unmatchedFormals.listIterator(); formalIt.hasNext(); ) {
      PairListExp formal = formalIt.next();
      if(formal.hasTag()) {
        SymbolExp name = (SymbolExp) formal.getTag();
        Collection<PairListExp> matches = Collections2.filter(unmatchedActuals, Predicates.matches(name));

        if(matches.size() == 1) {
          PairListExp match = first(matches);
          innerEnv.setVariable(name, new PromiseExp( match.getValue(), innerEnv ));
          formalIt.remove();
          unmatchedActuals.remove(match);

        } else if(matches.size() > 1) {
          throw new EvalException(String.format("Multiple named values provided for argument '%s'", name.getPrintName()));
        }
      }
    }

    // do partial matching
    Collection<PairListExp> remainingNamedFormals = filter(unmatchedFormals, Predicates.hasTag());
    for(Iterator<PairListExp> actualIt = unmatchedActuals.iterator(); actualIt.hasNext(); ) {
      PairListExp actual = actualIt.next();
      if(actual.hasTag()) {
        Collection<PairListExp> matches = Collections2.filter(remainingNamedFormals, Predicates.startsWith(actual.getTag()));

        if(matches.size() == 1) {
          PairListExp match = first(matches);
          innerEnv.setVariable(match.getTag(), new PromiseExp( actual.getValue(), innerEnv ));
          actualIt.remove();
          unmatchedFormals.remove(match);

        } else if(matches.size() > 1) {
          throw new EvalException(String.format("Provided argument '%s' matches multiple named formal arguments: %s",
              actual.getTag().getPrintName(), argumentTagList(matches)));
        }
      }
    }

    // match any unnamed args positionally

    Iterator<PairListExp> formalIt = unmatchedFormals.iterator();
    PeekingIterator<PairListExp> actualIt = Iterators.peekingIterator(unmatchedActuals.iterator());
    while( formalIt.hasNext()) {
      PairListExp formal = formalIt.next();
      if(formal.getTag().getPrintName().equals("...")) {
        PairListExp.Builder builder = new PairListExp.Builder();
        while(actualIt.hasNext()) {
          builder.add( new PromiseExp( actualIt.next().getValue(), innerEnv ) );
        }
        innerEnv.setVariable(formal.getTag(), builder.list());

      } else if( hasNextUnTagged(actualIt) ) {
        innerEnv.setVariable(formal.getTag(), new PromiseExp( nextUnTagged(actualIt).getValue(), innerEnv ) );

      } else if( formal.getValue() == SymbolExp.MISSING_ARG ) {
        innerEnv.setVariable(formal.getTag(), SymbolExp.MISSING_ARG);

      } else {
        innerEnv.setVariable(formal.getTag(), new PromiseExp( formal.getValue(), innerEnv )); // default
      }
    }
    if(actualIt.hasNext()) {
      throw new EvalException(String.format("Unmatched positional arguments"));
    }
  }

  private boolean hasNextUnTagged(PeekingIterator<PairListExp> it) {
    return it.hasNext() && !it.peek().hasTag();
  }

  private PairListExp nextUnTagged(Iterator<PairListExp> it) {
    PairListExp arg = it.next() ;
    while( arg.hasTag() ) {
      arg = it.next();
    }
    return arg;
  }

  private String argumentTagList(Collection<PairListExp> matches) {
    return Joiner.on(", ").join(transform(matches, new TagName()));
  }

  private static class TagName implements Function<PairListExp, String> {
    @Override
    public String apply(PairListExp input) {
      return ((SymbolExp)input.getTag()).getPrintName();
    }
  }

  private <X> X first(Iterable<X> values) {
    return values.iterator().next();
  }
}
