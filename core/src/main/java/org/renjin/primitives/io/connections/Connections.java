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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.annotations.Recycle;
import org.renjin.primitives.io.connections.Connection.Type;
import org.renjin.sexp.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.UnknownHostException;

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
  public static IntVector gzfile(@Current final Context context,
      final String path, String open, String encoding, double compressionLevel)
      throws IOException {

    return newConnection(context, open, new GzFileConnection(context.resolveFile(path)));
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
  public static IntVector file(@Current final Context context,
      final String path, String open, boolean blocking, String encoding) throws IOException {
    
    if(path.isEmpty()) {
      return newConnection(context, open, new SingleThreadedFifoConnection());
    } else if(STD_OUT.equals(path)) {
      return stdout(context);
    } else if(STD_IN.equals(path)) {
      return stdin(context);
    } else if(STD_ERR.equals(path)) {
      return stderr(context);
    } else {
      return newConnection(context, open, new FileConnection(context.resolveFile(path)));
    }
  }
  
  @Primitive
  public static IntVector url(@Current final Context context,
      final String description, String open, boolean blocking, String encoding) throws IOException {
  
    return newConnection(context, open, new UrlConnection(new URL(description)));
  }
  
  
  
  @Primitive
  public static IntVector stdin(@Current final Context context) {
    return terminal(ConnectionTable.STDIN_HANDLE);
  }

  @Primitive
  public static IntVector stdout(@Current final Context context) {
    return terminal(ConnectionTable.STDOUT_HANDLE);
  }

  public static IntVector stderr(@Current Context context) {
    return terminal(ConnectionTable.STDERR_HANDLE);
  }
  
  private static IntVector terminal(int index) {
    return new IntArrayVector(new int[] { index }, PairList.Node.singleton(Symbols.CLASS,
            new StringArrayVector("connection", "terminal")));
  }
  
  @Primitive("summary.connection")
  public static ListVector summaryConnection(@Current Context context, SEXP connHandle) {
    ListVector.NamedBuilder result = new ListVector.NamedBuilder();
    Connection connection = getConnection(context, connHandle);
    result.add("description", connection.getDescription());
    result.add("class", connection.getClassName());
    result.add("mode", connection.getMode());
    result.add("text", connection.getType() == Type.TEXT ? "text" : "binary");
    result.add("opened", connection.isOpen() ? "opened" : "closed");
    result.add("can read", connection.canRead() ? "yes" : "no");
    result.add("can write", connection.canWrite() ? "yes" : "no");
    return result.build();
  }

  @Primitive
  public static void close(@Current Context context, SEXP conn, String type /* Unused */)
      throws IOException {
    getConnection(context, conn).close();
  }

  public static String readChar(@Current Context context, SEXP connIndex, int nchars,
      @Recycle(false) boolean useBytes) throws IOException {

    Connection conn = getConnection(context, connIndex);
    
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
  public static StringVector readLines(@Current Context context, SEXP connection, int numLines, boolean ok, 
      boolean warn, String encoding) throws IOException {
    
    PushbackBufferedReader reader = getConnection(context, connection).getReader();
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
  public static void writeLines(@Current Context context, StringVector x, SEXP connIndex, String seperator, boolean useBytes) throws IOException {
    PrintWriter writer = getConnection(context, connIndex).getPrintWriter();
    for(String line : x) {
      writer.print(line);
      writer.print(seperator);
    }
  }
  
  //FIXME: port should be an int
  @Primitive("socketConnection")
  public static IntVector socketConnection(@Current Context context, String host, double port) throws UnknownHostException, IOException{
    return newConnection(context, "", new SocketConnection(host, (int) port));
  }
  
  @Primitive
  public static void sink(SEXP file, SEXP closeOnExit, SEXP arg2, SEXP split) {
    // todo: implement
  }
  
  @Primitive
  public static void open(@Current Context context, SEXP conn, String open, boolean blocking) throws IOException {
    getConnection(context, conn).open(new OpenSpec(open));    
  }
  
  @Primitive
  public static boolean isOpen(@Current Context context, SEXP conn, String rw) {
    //TODO: handle rw parameter
    return getConnection(context, conn).isOpen();
  }
  
  @Primitive
  public static void pushBack(@Current Context context, Vector data, SEXP connection, boolean newLine) throws IOException {
    PushbackBufferedReader reader = getConnection(context, connection).getReader();
    String suffix = newLine ? "\n" : "";
    for(int i=data.length()-1;i>=0;--i) {
      if(data.isElementNA(i)) {
        reader.pushBack("NA" + suffix);
      } else {
        reader.pushBack(data.getElementAsString(i) + suffix);
      }
    }
  }
  
  @Primitive
  public static int pushBackLength(@Current Context context, SEXP connection) throws IOException {
    PushbackBufferedReader reader = getConnection(context, connection).getReader();
    return reader.countLinesPushedBack();
  }
  
  
  public static Connection getConnection(Context context, SEXP conn) {
    if(!conn.inherits("connection") || !(conn instanceof Vector) || conn.length() != 1) {
      throw new EvalException("'con' is not a connection");
    }
    int connIndex = ((Vector)conn).getElementAsInt(0);
    return context.getGlobals().getConnectionTable().getConnection(connIndex);
  }

  private static IntVector newConnection(final Context context, String open, Connection conn) throws IOException, FileSystemException {
    if(!Strings.isNullOrEmpty(open)) {
      conn.open(new OpenSpec(open));
    }
    return context.getGlobals().getConnectionTable().newConnection(conn);
  }
}
