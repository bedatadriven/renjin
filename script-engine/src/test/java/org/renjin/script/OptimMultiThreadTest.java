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

package org.renjin.script;

import org.junit.Test;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ListVector;

import javax.script.ScriptEngine;

public class OptimMultiThreadTest {
  private static final ThreadLocal<ScriptEngine> engines = new ThreadLocal<>();
  private static final int RUNS = 20;
  private static final int THREADS_NUMBER = 2;


  @Test
  public void testMultithread() throws InterruptedException {
    Thread[] threads = new Thread[THREADS_NUMBER];
    for (int i = 0; i < THREADS_NUMBER; i++) {
      threads[i] = new Thread(this::task);
      threads[i].start();
    }
    for (int i = 0; i < THREADS_NUMBER; i++) {
      threads[i].join();

    }
  }

  private void task() {
    ScriptEngine engine = getEngine();
    try {
      // Rosenbrock Banana function
      engine.eval("fr <- function(x) { " +
          " x1 <- x[1]\n" +
          " x2 <- x[2]\n" +
          " 100 * (x2 - x1 * x1) ^ 2 + (1 - x1) ^ 2\n" +
          "}");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    for (int i = 0; i < RUNS; i++) {
      try {
        ListVector result = (ListVector) engine.eval("optim(c(-1.2,1), fr, method = \"L-BFGS\")");
        System.out.println(result);

        DoubleVector parameters = (DoubleVector) result.get("par");
        double p0 = parameters.getElementAsDouble(0);
        double p1 = parameters.getElementAsDouble(1);
        if(Math.abs(p0 - 0.9998000) > 0.00001) {
          throw new AssertionError("Incorrect result: p0 = " + String.format("%f", p0));
        }
        if(Math.abs(p1 - 0.9996001) > 0.00001) {
          throw new AssertionError("Incorrect result: p1 = " + String.format("%f", p1));
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
    }
  }

  private ScriptEngine getEngine() {
    ScriptEngine engine = engines.get();
    if (engine == null) {
      RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
      engine = factory.getScriptEngine();
      engines.set(engine);
    }
    return engine;
  }

}
