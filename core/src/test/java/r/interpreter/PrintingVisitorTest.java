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

package r.interpreter;

import org.junit.Test;
import r.lang.DoubleExp;
import r.lang.IntExp;
import r.lang.PairListExp;
import r.lang.StringExp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PrintingVisitorTest {

  @Test
  public void realVector() {
    assertThat(new PrintingVisitor(new DoubleExp(1,99,3), 80).getResult(),
        equalTo("[1]  1 99  3\n"));                                                
  }

  @Test
  public void stringVector() {
    assertThat(new PrintingVisitor(new StringExp("abcdef", "a", "b"), 80).getResult(),
        equalTo("[1] \"abcdef\" \"a\"      \"b\"     \n"));
  }

  @Test
  public void listOfVectors() {
    assertThat(new PrintingVisitor(
        PairListExp.fromArray(new DoubleExp(1), new IntExp(999, 1), new StringExp("hello world")), 80)
          .getResult(),
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

}
