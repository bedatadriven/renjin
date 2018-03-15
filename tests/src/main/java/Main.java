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
import org.renjin.eval.EvalException;

import javax.script.*;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Main class which executes a single test, useful for debugging
 * individual tests in Java IDEs
 */
public class Main {

  public static void main(String[] args) throws FileNotFoundException, ScriptException, NoSuchMethodException {
    String file = args[0];

    // create a script engine manager
    ScriptEngineManager factory = new ScriptEngineManager();

    try {
      // create an R engine
      ScriptEngine engine = factory.getEngineByName("Renjin");
      engine.eval(new FileReader("src/test/R/" + file));

      Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
      for (String name : bindings.keySet()) {
        if (name.startsWith("test.")) {
          Invocable invocable = (Invocable) engine;
          invocable.invokeFunction(name);
        }
      }
    } catch (EvalException e) {
      System.out.println("ERROR: " + e.getMessage());
      e.printRStackTrace(System.out);
      throw e;
    }
  }
}
