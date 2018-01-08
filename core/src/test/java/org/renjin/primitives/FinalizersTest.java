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
package org.renjin.primitives;


import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;

public class FinalizersTest {

  @Test
  public void test() {

    Session session = SessionBuilder.buildDefault();

    // I don't want to insert calls to run finalizers everywhere
    // so far now it's up to the Renjin caller to invoke runFinalizers()
    // periodically if needed.

    session.runFinalizers();
    session.getTopLevelContext().evaluate(RParser.parseSource(
        "reg.finalizer(.GlobalEnv, f=function(e) cat(environmentName(e)), onexit=TRUE);"));
    session.runFinalizers();
    session.getTopLevelContext().evaluate(RParser.parseSource("gc();"));

    // Finalizers with onexit = TRUE will run when the session is closed
    session.close();
  }

}