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
import org.junit.Test;
import org.renjin.sexp.DoubleArrayVector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MultithreadInitTest {

  private static DoubleArrayVector args = new DoubleArrayVector(1,2,3,4,5);

  static class RunClazz implements Runnable {

    @Override
    public void run() {
      System.out.println("thread ID: " + Thread.currentThread().getId());
      try {
        ScriptEngineManager factory = new ScriptEngineManager();

        ScriptEngine engine = factory.getEngineByName("Renjin");

        engine.put("x", args);
        engine.eval("y <- mean(x)");
        Object result = engine.get("y");

        assertThat(((DoubleArrayVector)result).get(0), equalTo(3d));
      } catch(Exception e) {
        e.printStackTrace(new PrintStream(System.err));
      }
    }
  }

  @Test
  public void test() throws InterruptedException {
    int nThreads = 10;
    Thread[] workers = new Thread[nThreads];
    for(int i = 0; i < nThreads; i++) {
      workers[i] = new Thread(new RunClazz());
      workers[i].start();
    }

    for(int i = 0; i < nThreads; i++) {
      workers[i].join();
    }

  }

}
