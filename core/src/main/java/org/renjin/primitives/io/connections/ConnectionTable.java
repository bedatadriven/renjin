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

import org.renjin.eval.EvalException;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbols;

import java.io.IOException;

/**
 * Maintains a table of per-Session connection objects.
 * 
 * <p>Note that this is maybe not the best design, it 
 * certainly introduces threading issues to think about,
 * but R exposes connections to R code as integers, and
 * there is code that depends on that fact.
 */
public class ConnectionTable {
  
  public static final int STDIN_HANDLE = 0;
  public static final int STDOUT_HANDLE = 1;
  public static final int STDERR_HANDLE = 2;
 
  
  private StdInConnection stdin;
  private StdOutConnection stdout;
  private StdOutConnection stderr;
  
  private static final int NUM_CONNECTIONS = 128;
  private Connection[] table = new Connection[NUM_CONNECTIONS];
  
  
  public ConnectionTable() {
    table[STDIN_HANDLE] = stdin = new StdInConnection();
    table[STDOUT_HANDLE] = stdout = new StdOutConnection();
    table[STDERR_HANDLE] = stderr = new StdOutConnection(); //TODO Stderr
  }
  
  public IntVector newConnection(Connection conn) {
    IntArrayVector.Builder sexp = new IntArrayVector.Builder(1);
    sexp.set(0, installConnection(conn));
    sexp.setAttribute(Symbols.CLASS, new StringArrayVector("connection", conn.getClassName()));
    return sexp.build();
  }


  public void close(int index) throws IOException {
    table[index].close();
    table[index] = null;
  }


  public void close(Connection connection) throws IOException {
    for (int i = 0; i < table.length; i++) {
      if(table[i] == connection) {
        table[i].close();
        table[i] = null;
      }
    }

  }
  
  private int installConnection(Connection conn) {
    for(int i=0;i!=table.length;++i) {
      if(table[i] == null) {
        table[i] = conn;
        return i;
      }
    }
    throw new EvalException("maximum number of connections exceeded");
  }

  public Connection getConnection(int index) {
    if(index >= table.length || table[index] == null) {
      throw new EvalException("invalid connection");
    }
    return table[index];
  }
  
  public Connection getConnection(IntVector conn) {
    return getConnection(conn.getElementAsInt(0));
  }
  
  public StdInConnection getStdin() {
    return stdin;
  }

  public StdOutConnection getStdout() {
    return stdout;
  }

  public StdOutConnection getStderr() {
    return stderr;
  }

}
