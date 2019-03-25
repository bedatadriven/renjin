/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.aot;

import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.sexp.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClosureCompilerTest {

  @Test
  public void simpleTest() throws InvocationTargetException, IllegalAccessException {

    Session session = new SessionBuilder().build();
    Closure closure = (Closure) session.getTopLevelContext().evaluate(Symbol.get("scale.default"));

    ClosureCompiler compiler = new ClosureCompiler(session, closure);

    Method method = compiler.getHandle().loadAndReflect();

    DoubleVector x = new DoubleArrayVector(1, 2, 3);

    SEXP result = (SEXP) method.invoke(session.getTopLevelContext(), session.getGlobalEnvironment(), new SEXP[]{x, LogicalVector.TRUE, LogicalVector.FALSE});

    System.out.println(result);


  }

}