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
package org.renjin.cli.build;

import io.airlift.airline.ParseException;
import org.renjin.packaging.GimpleCompilationException;

public abstract class BuildCommand implements Runnable {

  @Override
  public final void run() {
    try {
      tryRun();
    } catch (ParseException e) {
      System.err.println("renjin: " + e.getMessage());
      System.err.println("Try 'renjin help' for more information.");

    } catch (GimpleCompilationException e) {
      System.err.println("Gimple compilation failed. ");
      e.printStackTrace(System.err);
      System.err.println();
      System.err.println("Renjin tried to compile this package's native sources (C, C++, or Fortran)");
      System.err.println("to Java classes, but encountered a problem. See the stacktrace above for details.");
      System.err.println();
      System.err.println("You can run again with the --ignore-gimple-errors, but you may encounter");
      System.err.println("errors at runtime if you try to use a function that renjin couldn't compile.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected abstract void tryRun() throws Exception;
}
