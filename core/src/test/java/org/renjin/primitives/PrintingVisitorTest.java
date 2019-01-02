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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PrintingVisitorTest extends EvalTestCase {

  @Test
  public void realVector() {
    assertThat(print(new DoubleArrayVector(1, 99, 3)), equalTo("[1]  1 99  3\n"));
  }

  @Test
  public void stringVector() {
    assertThat(print(new StringArrayVector("abcdef", "a", "b")), equalTo("[1] \"abcdef\" \"a\"      \"b\"     \n"));
  }

  @Test
  public void listOfVectors() {
    assertThat(print(
        new ListVector(new DoubleArrayVector(1), new IntArrayVector(999, 1), StringVector.valueOf("hello world"))),
        equalTo("[[1]]\n" +
                "[1] 1\n" +
                "\n" +
                "[[2]]\n" +
                "[1] 999   1\n" +
                "\n" +
                "[[3]]\n" +
                "[1] \"hello world\"\n" +
                "\n"));
  }


  private String print(SEXP x) {
    return new Print.PrintingVisitor(topLevelContext).print(x);
  }


}
