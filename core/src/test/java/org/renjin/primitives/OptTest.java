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
package org.renjin.primitives;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import static org.junit.Assert.assertThat;


public class OptTest extends EvalTestCase{
  

  @Test
  public void overloadingWorks() {
    // this will fail if they are fed thru the string overload
    assertThat(eval("10>5"), elementsIdenticalTo(c(true)));
    assertThat(eval("10L>5L"), elementsIdenticalTo(c(true)));
    assertThat(eval("TRUE>FALSE"), elementsIdenticalTo(c(true)));
    assertThat(eval("'one' > 'zed'"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void typePreservedAndNotMangledByImplicitCasting() {
    assertThat(eval("-1L"), elementsIdenticalTo(c_i(-1)));
  }
  
  @Test(expected=EvalException.class)
  public void vectorsAreChecked() {
    eval("sqrt('4')");
  }
  
  @Test
  public void integerDivision() {
    assertThat( eval("7 %/% 3"), elementsIdenticalTo(c(2)));
    assertThat( eval("7 %/% 3.9"), elementsIdenticalTo(c(1)));
    assertThat( eval("-7 %/% 3.9"), elementsIdenticalTo(c(-2)));
  }
  
  @Test
  public void dimAttribIsCopied() {
    eval(" y <- c(1,2) ");
    eval(" dj <- c(1,1) ");
    eval(" dim(dj) <- c(2,1)");
    
    assertThat(eval(" dim( y / dj )"), elementsIdenticalTo(c_i(2,1)));
  }
  
  @Test
  public void symbolEquality() {
    assertThat(eval("quote(x)==quote(y)"), elementsIdenticalTo(c(false)));
    assertThat(eval("quote(x)==quote(x)"), elementsIdenticalTo(c(true)));
    assertThat(eval("quote(a)<quote(b)"), elementsIdenticalTo(c(true)));
  }
  
  @Test
  public void stringComparison() {
    assertThat(eval("'a' < 'b'"), elementsIdenticalTo(c(true)));
  }
  
  @Test
  public void languageEquality() {
    assertThat(eval("quote(c(1)) == 'c(1)'"), elementsIdenticalTo(c(true)));
  }
  
  @Ignore("this makes no sense to me")
  @Test
  public void listComparison() {
    assertThat(eval("list(1) == 1"), elementsIdenticalTo(c(true)));
  }
  
  
  @Test
  public void complexAdd() {
    assertThat(eval("3+1i"), elementsIdenticalTo(c(complex(3,1))));
  }
  
  @Test
  public void negatedLogical() {
    topLevelContext.getEnvironment().setVariable(topLevelContext, Symbol.get("x"), LogicalVector.EMPTY);
    assertThat(eval("!x"), identicalTo((SEXP)LogicalVector.EMPTY));
  }
  
  @Test
  public void testFunctionCallEqual() {
    eval("x <- ~0 + births");
    assertThat(eval("x == x"), elementsIdenticalTo(c(true)));
    
  }
  
}
