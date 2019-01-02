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
package org.renjin.script;

import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;

public class ThreadTest {

  @Test
  public void testGraphicsInNewThread() throws IOException, ScriptException, InterruptedException {

    // Start a new session in the current thread
    RenjinScriptEngine engine = new RenjinScriptEngineFactory().getScriptEngine();
    engine.eval("f <- tempfile()");


    // Define a task that can be run in a new thread
    Runnable plotTask = () -> {
      try {
        engine.eval("png(f)");
        engine.eval("plot(1:12)");
        engine.eval("dev.off()");
      } catch (ScriptException e) {
        throw new RuntimeException(e);
      }
    };

    // Start a new thread, run the task, and wait for it to complete
    Thread thread = new Thread(plotTask);
    thread.start();
    thread.join();

    // Verify that file is written
    engine.eval("stopifnot(file.exists(f))");

  }
}
