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
package org.renjin.compiler.cfg;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.sexp.Closure;

import java.util.Collections;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class InlinedFunctionTest extends EvalTestCase {

  
  @Test
  public void simple() {
    InlinedFunction fn = compileFunction("function() 42");

    ValueBounds returnBounds = fn.computeBounds();
    
    assertTrue(returnBounds.isConstant());
    assertThat(returnBounds.getConstantValue(), elementsIdenticalTo(c(42)));
  }

  @Test
  public void argument() {
    InlinedFunction fn = compileFunction("function(x = 1) length(x)");

    System.out.println(fn.getCfg());
    
    ValueBounds returnBounds = fn.computeBounds();

    assertTrue(returnBounds.isConstant());
    assertThat(returnBounds.getConstantValue(), elementsIdenticalTo(c_i(1)));
  }
  
  private InlinedFunction compileFunction(String functionDecl) {
    Closure closure = (Closure) eval(functionDecl);
    return new InlinedFunction(new RuntimeState(topLevelContext, topLevelContext.getGlobalEnvironment()),
        closure, Collections.emptySet());
  }


}