/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.renjin.primitives.io.connections;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.UnknownHostException;

import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.annotations.Recycle;

import r.lang.Context;
import r.lang.ExternalExp;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.exception.EvalException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

/**
 * 
 * Functions which create and manipulates connection objects.
 * 
 * <p>
 * Connection objects are {@link ExternalExp}s that hold a reference to a class
 * implementing the {@link Connection} interface.
 * 
 */
public class Connections {

  private static final String STD_OUT = "stdout";
  private static final String STD_IN = "stdin";
  private static final String STD_ERR = "stderr";
  
  /**
   * Opens a connection to a gzipped file.
   * 
   * @param context
   *          the current call Context
   * @param path
   *          path to the gzipped file
   * @param open
   *          the mode flag
   * @param encoding
   *          the character encoding if the file is to be opened for text
   *          reading
   * @param compressionLevel
   *          integer 0-9
   * @return an external reference object which inherits from the (S3) class
   *         "connection"
   * @throws IOException 
   */
  public static ExternalExp<Connection> gzfile(@Current final Context context,
      final String path, String open, String encoding, double compressionLevel)
      throws IOException {

    return asSexp(new GzFileConnection(context.resolveFile(path), open));
  }
  
  /**
   * Opens a connection to a file.
   * 
   * @param context
   *          the current Context
   * @param path
   *          path to the file
   * @param open
   *          the mode flag that determines how the file
   * @param blocking
   *          In blocking mode, functions using the connection do not return to
   *          the R evaluator until the read/write is complete. In non-blocking
   *          mode, operations return as soon as possible, so on input they will
   *          return with whatever input is available (possibly none) and for
   *          output they will return whether or not the write succeeded.
   * 
   * @param encoding
   *          the character to encoding, if the file is to be opened as text
   * @return an external reference object which inherits from the (S3) class
   *         "connection"
   * @throws IOException 
   */
  public static ExternalExp<Connection> file(@Current final Context context,
      final String path, String open, boolean blocking, String encoding) throws IOException {
    
    if(path.isEmpty()) {
      return asSexp(new SingleThreadedFifoConnection());
    } else if(STD_OUT.equals(path)) {
      return stdout(context);
    } else if(STD_IN.equals(path)) {
      return stdin(context);
    } else if(STD_ERR.equals(path)) {
      return stderr(context);
    } else {
      return asSexp(new FileConnection(context.resolveFile(path), open));
    }
  }

  
  @VisibleForTesting
  static ExternalExp<Connection> asSexp(Connection conn) {
    return new ExternalExp<Connection>(conn, "connection");
  }
  
  @Primitive
  public static ExternalExp<Connection> stdin(@Current final Context context) {
    return asSexp(new StdInConnection(context));
  }

  @Primitive
  public static ExternalExp<Connection> stdout(@Current final Context context) {
    return asSexp(new StdOutConnection(context));
  }

  public static ExternalExp<Connection> stderr(@Current Context context) {
    return asSexp(new StdOutConnection(context));
  }

  @Primitive
  public static void close(Connection conn, String type /* Unused */)
      throws IOException {
    conn.close();
  }

  public static String readChar(Connection conn, int nchars,
      @Recycle(false) boolean useBytes) throws IOException {

    if(useBytes) {
      byte[] bytes = new byte[nchars];
      DataInputStream dis = new DataInputStream(conn.getInputStream());
      dis.readFully(bytes);
      return new String(bytes, Charsets.UTF_8);
    } else {
      
      // it's not clear to me whether the read(char[]) methods are
      // safe to use with unicode...
      Reader in = conn.getReader();
      StringBuilder result = new StringBuilder();
      for(int i=0;i!=nchars;++i) {
        result.appendCodePoint(in.read());
      }
      return result.toString();
    }
  }

  @Primitive("readLines")
  public static StringVector readLines(Connection connection, int numLines, boolean ok, 
      boolean warn, String encoding) throws IOException {
    
    BufferedReader reader = connection.getReader();
    StringVector.Builder lines = new StringVector.Builder();
    String line;
    while((line=reader.readLine())!=null && 
        (lines.length() != numLines)) {
      lines.add(line);
    }
    
    if(numLines > 0 && 
       lines.length() < numLines && 
       !ok) {
      
      throw new EvalException("too few lines read in readLines");
    }
    
    return lines.build();
  }
  
  @Primitive("writeLines")
  public static void writeLines(StringVector x, Connection connection, String seperator, boolean useBytes) throws IOException {
    PrintWriter writer = connection.getPrintWriter();
    for(String line : x) {
      writer.print(line);
      writer.print(seperator);
    }
  }
  
  //FIXME: port should be an int
  @Primitive("socketConnection")
  public static ExternalExp<Connection> socketConnection(String host, double port) throws UnknownHostException, IOException{
    return asSexp(new SocketConnection(host, (int) port));
  }
  
  @Primitive
  public static void sink(SEXP file, SEXP closeOnExit, SEXP arg2, SEXP split) {
    // todo: implement
  }
  
  @Primitive
  public static boolean isOpen(Connection conn, String rw) {
    //TODO: handle rw parameter
    return conn.isOpen();
  }
}
