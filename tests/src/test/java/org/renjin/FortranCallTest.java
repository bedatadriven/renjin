/*
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
package org.renjin;

import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

import javax.script.ScriptException;
import java.util.concurrent.Executors;

public class FortranCallTest {

  
  @Test
  public void test() throws ScriptException {
    Session session = new SessionBuilder()
        .setExecutorService(Executors.newFixedThreadPool(4))
        .build();

    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine scriptEngine = factory.getScriptEngine(session);

    scriptEngine.eval("library(stats)");
    scriptEngine.eval("x <- (1:1000 + (1:10 * 3))");
    scriptEngine.eval("k <- kmeans(x, 3)");
    scriptEngine.eval("y <- x * k$withinss / 3 * 4");
    scriptEngine.eval("print(mean(y))");
  }
}
