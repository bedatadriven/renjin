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
package org.renjin.primitives.io.connections;

import java.io.PrintWriter;

/**
 * A sink on a connection
 */
class Sink {


  /**
   * The connection to which the output should be sunk.
   */
  private Connection connection;

  /**
   * If {@code true}, output will be sent to the new sink and
   * to the current output stream, like the Unix program ‘tee’.
   */
  private boolean split;


  /**
   * If {@code true}, the sink should be closed when the sink is cleared.
   */
  private boolean closeOnExit;


  public Sink(Connection connection, boolean split, boolean closeOnExit) {
    this.connection = connection;
    this.split = split;
    this.closeOnExit = closeOnExit;
  }

  public PrintWriter getPrintWriter(PrintWriter originalStream) {
    PrintWriter sinkWriter = connection.getOpenPrintWriter();
    if(split) {
      return new PrintWriter(new SplitWriter(originalStream, sinkWriter));
    } else {
      return sinkWriter;
    }
  }

  public boolean isCloseOnExit() {
    return closeOnExit;
  }

  public Connection getConnection() {
    return connection;
  }
}
