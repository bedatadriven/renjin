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
package org.renjin;

import org.junit.Before;
import org.junit.BeforeClass;
import org.renjin.primitives.Deparse;
import org.renjin.primitives.Identical;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.SEXP;

import javax.script.ScriptException;

public abstract class AbstractMicroTest {

  public static RenjinScriptEngine engine;

  @BeforeClass
  public static void setupScriptEngine() {
    engine = new RenjinScriptEngineFactory().getScriptEngine();
  }

  @Before
  public void cleanUpGlobalEnvironment() {
    engine.getSession().getGlobalEnvironment().clear();
  }

  protected final void assertIdentical(String x, String y) {
    SEXP xexp;
    try {
      xexp = (SEXP)engine.eval(x);
    } catch (ScriptException e) {
      throw new RuntimeException("Error evaluating: " + x, e);
    }

    SEXP yexp = null;
    try {
      yexp = (SEXP)engine.eval(y);
    } catch (ScriptException e) {
      throw new RuntimeException("Error evaluating: " + y, e);
    }

    if(!Identical.identical(xexp, yexp)) {
      throw new AssertionError("Expecting '" + y + "', got: '" +
        Deparse.deparse(engine.getTopLevelContext(), xexp, 80, false, 0, 0) + "'");
    }
  }

}
