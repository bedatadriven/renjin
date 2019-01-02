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
package org.renjin.gcc.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Simple logger for compilation diagnostics
 */
public class Logger {

  public static final Logger NULL = new Logger();

  private File file = null;

  private PrintWriter printWriter = null;

  private Logger() {
    file = null;
  }

  Logger(File file) {
    this.file = file;
  }

  public void log(String message) {
    if(file == null) {
      return;
    }
    if(printWriter == null) {
      try {
        printWriter = new PrintWriter(file);
      } catch (FileNotFoundException e) {
        System.err.println("Could not open log at " + file + ": " + e.getMessage());
        file = null;
        return;
      }
    }

    printWriter.println(message);
  }

  void close() {
    if(printWriter != null) {
      printWriter.close();
      printWriter = null;
    }
  }

}
