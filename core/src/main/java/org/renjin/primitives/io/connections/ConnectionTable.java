package org.renjin.primitives.io.connections;

import org.renjin.eval.EvalException;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbols;

/**
 * Maintains a list of per-Apartment connections.
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
