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
package org.renjin.compiler.ir.tac;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.Closure;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ApplyCallCompilerTest extends EvalTestCase {

  @Test
  public void test() {

    Closure function = (Closure) eval("function(x) x * 2");
    Vector vector = (Vector)eval("1:1000");

    ApplyCallCompiler compiler = new ApplyCallCompiler(topLevelContext, global, function, vector);
    compiler.tryCompile();

    assertThat(compiler.getFunctionBounds(), equalTo(ValueBounds.primitive(TypeSet.DOUBLE)));
    assertThat(compiler.isPure(), equalTo(true));



  }

}