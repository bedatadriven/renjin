/**
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
package org.renjin.compiler.ir.tac;

import org.junit.Test;
import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.EvalException;
import org.renjin.eval.MatchedArgumentPositions;
import org.renjin.sexp.PairList;
import org.renjin.sexp.Symbol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MatchedArgumentPositionsTest {

  private static final String UNNAMED = null;
  
  @Test
  public void positionalMatching() {

    MatchedArgumentPositions match = match(args("x", "y"), args(UNNAMED, UNNAMED));
    assertThat(match.getActualIndex(0), equalTo(0));
    assertThat(match.getActualIndex(1), equalTo(1));
  }


  @Test
  public void exactName() {

    MatchedArgumentPositions match = match(args("x", "y"), args("y", "x"));
    assertThat(match.getActualIndex(0), equalTo(1));
    assertThat(match.getActualIndex(1), equalTo(0));
  }


  @Test
  public void oneExactName() {

    MatchedArgumentPositions match = match(args("x", "y"), args("y", UNNAMED));
    assertThat(match.getActualIndex(0), equalTo(1));
    assertThat(match.getActualIndex(1), equalTo(0));
  }

  @Test
  public void partialMatching() {

    MatchedArgumentPositions match = match(args("foo", "bar"), args("b", "f"));
    assertThat(match.getActualIndex(0), equalTo(1));
    assertThat(match.getActualIndex(1), equalTo(0));
  }


  @Test
  public void partialMatching3() {

    MatchedArgumentPositions match = match(args("x", "foo", "bar"), args("b", "f"));
    assertThat(match.getActualIndex(0), equalTo(-1));
    assertThat(match.getActualIndex(1), equalTo(1));
    assertThat(match.getActualIndex(2), equalTo(0));
  }

  @Test
  public void eval() {
    MatchedArgumentPositions match = match(args("z", "..."), args("x"));
    assertThat(match.getActualIndex(0), equalTo(-1));
    assertThat(match.getExtraArgumentCount(), equalTo(1));

  }


  @Test(expected = EvalException.class)
  public void duplicateExactArgs() {
    match(args("x", "y"), args("x", "x"));
  }

  @Test(expected = EvalException.class)
  public void duplicatePartialArgs() {
    match(args("aa", "aaa"), args("a", "x"));
  }

  @Test
  public void noPartialMatchingAfterElipses() {
    MatchedArgumentPositions match = match(args("x", "...", "protocal"), args("x", "p", UNNAMED));
    assertThat(match.getActualIndex(0), equalTo(0));
    assertThat(match.getActualIndex(2), equalTo(-1));
    assertThat(match.getExtraArgumentCount(), equalTo(2));

  }

  @Test(expected = EvalException.class)
  public void extraNamed() {
    match(args("x"), args("y"));

  }

  @Test
  public void extraArguments() {
    MatchedArgumentPositions match = match(args("x", "..."), args(UNNAMED, UNNAMED, UNNAMED, UNNAMED));
    assertThat(match.getActualIndex(0), equalTo(0));
    assertThat(match.getExtraArgumentCount(), equalTo(3));
  }


  private MatchedArgumentPositions match(String[] formalNames, String[] actuals) {

    PairList.Node.Builder formals = new PairList.Builder();
    for (String formalName : formalNames) {
      formals.add(formalName, Symbol.MISSING_ARG);
    }

    return new ArgumentMatcher(formals.build()).match(actuals);
  }


  private String[] args(String... names) {
    return names;
  }
}