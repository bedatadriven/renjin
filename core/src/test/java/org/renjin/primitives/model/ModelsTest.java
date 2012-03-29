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

package org.renjin.primitives.model;

import java.io.IOException;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class ModelsTest extends EvalTestCase {

  @Test
  public void simplestTest() {
      
    eval(" formula <- ~1 ");
    eval(" t <- .Internal(terms.formula(formula,NULL,NULL, FALSE,FALSE))");

    assertThat( eval(" t "), equalTo((SEXP)FunctionCall.newCall(Symbol.get("~"), new DoubleVector(1))));
    assertThat( eval(" attr(t, 'variables')"), equalTo(call("list")));
    assertThat( eval(" attr(t, 'factors')"), equalTo((SEXP)new IntVector()));
    assertThat( eval(" .Internal(environment(t)) "), sameInstance((SEXP)topLevelContext.getGlobalEnvironment()));
  }

  @Test
  public void testWithOneDepVar() throws IOException {
    assumingBasePackagesLoad();
    
    eval(" t <- terms(~births)");

    eval("print(t)");
    
    assertThat( eval(" class(t) "), equalTo(c("terms", "formula")));
    assertThat( eval(" attr(t, 'variables') "), equalTo(call("list", Symbol.get("births"))));
    assertThat( eval(" attr(t, 'term.labels') "), equalTo(c("births")));
    assertThat( eval(" attr(t, 'factors')"), equalTo((SEXP)new IntVector(1)));
  }

  @Test
  public void modelMatrixWithInteractions() throws IOException {
    assumingBasePackagesLoad();
    
    eval("data <- data.frame(age=c(18,20,22,25), height=c(110,100,75,120), row.names=c('a','b','c','d'))");
    eval("m <- model.matrix(~ age * height, data=data)");
    
    assertThat(eval("dim(m)"), equalTo(c_i(4,4)));
    assertThat(eval("m"), equalTo(c(1,1,1,1,18,20,22,25,110,100,75,120,1980,2000,1650,3000)));
    
    assertThat(eval("colnames(m)"), equalTo(c("(Intercept)", "age", "height", "age:height")));
    assertThat(eval("rownames(m)"), equalTo(c("a","b","c","d")));
    
    eval("print(m)");
    
  }

  @Test
  public void modelMatrixSimple() throws IOException {
    assumingBasePackagesLoad();
    
    eval("data <- data.frame(age=c(18,20,22,25), height=c(110,100,75,120), row.names=c('a','b','c','d'))");
    eval("m <- model.matrix(~ age + height, data=data)");
    
    assertThat(eval("dim(m)"), equalTo(c_i(4,3)));
    assertThat(eval("m"), equalTo(c(1,1,1,1,18,20,22,25,110,100,75,120)));
    assertThat(eval("colnames(m)"), equalTo(c("(Intercept)", "age", "height")));
    assertThat(eval("rownames(m)"), equalTo(c("a","b","c","d")));
    
    eval("print(m)");
    
  }
  
  private SEXP call(String function, SEXP... arguments) {
    return FunctionCall.newCall(Symbol.get(function), arguments);
  }

}
