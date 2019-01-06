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
package org.renjin.primitives.io.connections;

import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Recycle;
import org.renjin.primitives.Identical;
import org.renjin.primitives.io.connections.Connection.Type;
import org.renjin.primitives.text.RCharsets;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.sexp.*;

import java.io.*;
import java.net.URL;

/**
 * 
 * Functions which create and manipulates connection objects.
 * 
 * <p>
 * Connection objects in GNU R are actually integer vectors which refer to an entry in a global
 * connection table. Unfortunately, there seems to be at least some code out there in the wild
 * that relies on this implementation detail
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
  @Internal
  public static IntVector gzfile(@Current final Context context,
      final String path, String open, String encoding, double compressionLevel)
      throws IOException {

    return newConnection(context, open, new GzFileConnection(context.resolveFile(path), RCharsets.getByName(encoding)));
  }

  @Internal
  public static IntVector xzfile(@Current final Context context,
                                 final String path, String open, String encoding, double compressionLevel)
      throws IOException {

    return newConnection(context, open, new XzFileConnection(context.resolveFile(path), RCharsets.getByName(encoding)));
  }


  @Internal
  public static IntVector bzfile(@Current final Context context,
                                 final String path, String open, String encoding, double compressionLevel)
      throws IOException {

    return newConnection(context, open, new BzipFileConnection(context.resolveFile(path), RCharsets.getByName(encoding)));
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
  @Internal
  public static IntVector file(@Current final Context context,
                               final String path, String open, boolean blocking, String encoding,
                               boolean raw) throws IOException {

    if(path.isEmpty()) {
      return newConnection(context, open, new SingleThreadedFifoConnection());
    } else if(STD_OUT.equals(path)) {
      return stdout(context);
    } else if(STD_IN.equals(path)) {
      return stdin(context);
    } else if(STD_ERR.equals(path)) {
      return stderr(context);
    } else if (path.startsWith("http://") || path.startsWith("https://")) {
      return url(context, path, open, blocking, encoding);
    } else {
      return newConnection(context, open, new FileConnection(context.resolveFile(path), RCharsets.getByName(encoding)));
    }
  }
  
  @Internal
  public static IntVector url(@Current final Context context,
      final String description, String open, boolean blocking, String encoding) throws IOException {
  
    return newConnection(context, open, new UrlConnection(new URL(description), RCharsets.getByName(encoding)));
  }
  
  @Internal
  public static IntVector textConnection(@Current final Context context,
      String objectName, StringVector object, String open, Environment env, String type) throws IOException {

    OpenSpec openSpec = new OpenSpec(open);
    if(openSpec.forWriting()) {
      return newConnection(context, open, new WriteTextConnection(Symbol.get(object.asString()), env));
    } else {
      return newConnection(context, open, new ReadTextConnection(objectName, Joiner.on('\n').join(object)));
    }
  }
  
  
  @Internal
  public static IntVector stdin(@Current final Context context) {
    return terminal(ConnectionTable.STDIN_HANDLE);
  }

  @Internal
  public static IntVector stdout(@Current final Context context) {
    return terminal(ConnectionTable.STDOUT_HANDLE);
  }

  @Internal
  public static IntVector stderr(@Current Context context) {
    return terminal(ConnectionTable.STDERR_HANDLE);
  }
  
  @Internal
  public static boolean isatty(@Current Context context, SEXP connHandle) {
    int connectionIndex = getConnectionIndex(connHandle);
    
    return (connectionIndex == ConnectionTable.STDIN_HANDLE || 
       connectionIndex == ConnectionTable.STDERR_HANDLE ||
        connectionIndex == ConnectionTable.STDOUT_HANDLE) &&
        context.getSession().getSessionController().isTerminal();
  }
  
  private static IntVector terminal(int index) {
    return new IntArrayVector(new int[] { index },
            AttributeMap.builder()
                      .setClass("connection", "terminal")
                      .build());

  }
  
  @Internal("summary.connection")
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

  @Internal
  public static void close(@Current Context context, SEXP conn, String type /* Unused */) throws IOException {
    close(context, conn);
  }

  public static void close(@Current Context context, SEXP conn) throws IOException {
    int connIndex = getConnectionIndex(conn);
    context.getSession().getConnectionTable().close(connIndex);
  }

  @Internal
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

  @Internal("readLines")
  public static StringVector readLines(@Current Context context, SEXP connection, int numLines, boolean ok, 
      boolean warn, String encoding) throws IOException {
    
    PushbackBufferedReader reader = getConnection(context, connection).getReader();
    StringVector.Builder lines = new StringVector.Builder();
    String line;
    while((line=reader.readLine())!=null) {
      lines.add(line);
      if(numLines > 0 && lines.length() == numLines) {
        break;
      }
    }

    if(numLines > 0 &&
        lines.length() < numLines &&
        !ok) {

      throw new EvalException("too few lines read in readLines");
    }
    
    return lines.build();
  }

  @Internal
  public static Vector readBin(@Current Context context, SEXP connIndex, SEXP what, int n, int size, boolean signed, boolean swap) throws IOException {
    BinaryReader reader;
    if(connIndex instanceof RawVector) {
      reader = new BinaryReader((RawVector)connIndex);
    } else {
      Connection connection = getConnection(context, connIndex);
      InputStream inputStream = connection.getInputStream();
      reader = new BinaryReader(inputStream);
    }

    String typeName = what.asString();
    switch (typeName) {
      case "integer":
        return reader.readIntVector(n, size, signed, swap);
      case "double":
        return reader.readDoubleVector(n, size, swap);
      case "complex":
        return reader.readComplexVector(n, size, swap);
      case "character":
        return reader.readCharacterVector(n, size, swap);
      case "raw":
        return reader.readRaw(n, size);
      default:
        throw new EvalException("Unsupported/unimplemented type: " + typeName);
    }
  }

  @Internal
  public static void writeBin(@Current Context context, SEXP object, SEXP con, int size, boolean swap, boolean useBytes) throws IOException {
    if(con instanceof IntVector) {
      Connection connection = getConnection(context, con);
      if(object instanceof RawVector) {
        connection.getOutputStream().write(((RawVector) object).toByteArrayUnsafe());
      } else {
        throw new UnsupportedOperationException("TODO: typeof(object) = %s" + object.getTypeName());
      }
    } else {
      throw new EvalException("TODO: typeof(con) = %s" + con.getTypeName());
    }
  }


  @Internal("writeLines")
  public static void writeLines(@Current Context context, StringVector x, SEXP connIndex, String seperator, boolean useBytes) throws IOException {
    PrintWriter writer = getConnection(context, connIndex).getPrintWriter();
    for(String line : x) {
      writer.print(line);
      writer.print(seperator);
    }
    writer.flush();
  }
  
  //FIXME: port should be an int
  @Internal("socketConnection")
  public static IntVector socketConnection(@Current Context context, String host, double port) throws IOException{
    return newConnection(context, "", new SocketConnection(host, (int) port));
  }
  
  @Internal
  public static void sink(@Current Context context, SEXP connection,
                          boolean closeOnExit,
                          boolean messages,
                          boolean split) throws IOException {

    ConnectionTable table = context.getSession().getConnectionTable();

    StdOutConnection source;
    if(messages) {
      source = table.getStderr();
    } else {
      source = table.getStdout();
    }

    if(Identical.identical(connection, new IntArrayVector(-1))) {
      Sink sink = source.clearSink();
      if(sink != null && sink.isCloseOnExit()) {
        context.getSession().getConnectionTable().close(sink.getConnection());
      }
    } else {
      Connection sinkConnection = getConnection(context , connection);
      source.sink(new Sink(sinkConnection, split, closeOnExit));
    }
  }

  @Internal("sink.number")
  public static int sinkNumber(@Current Context context, boolean output) {
    StdOutConnection source;
    if(output) {
      source = context.getSession().getConnectionTable().getStdout();
    } else {
      source = context.getSession().getConnectionTable().getStderr();
    }
    return source.getSinkStackHeight();
  }
  
  @Internal
  public static void open(@Current Context context, SEXP conn, String open, boolean blocking) throws IOException {
    getConnection(context, conn).open(new OpenSpec(open));    
  }
  
  @Internal
  public static boolean isOpen(@Current Context context, SEXP conn, String rw) {
    //TODO: handle rw parameter
    return getConnection(context, conn).isOpen();
  }
  
  @Internal
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

  @Internal
  public static void flush(@Current Context context, SEXP connection) throws IOException {
    getConnection(context, connection).flush();
  }
  
  @Internal
  public static int pushBackLength(@Current Context context, SEXP connection) throws IOException {
    PushbackBufferedReader reader = getConnection(context, connection).getReader();
    return reader.countLinesPushedBack();
  }


  /**
   * Helper function which retrieves the {@link Connection} instance for a given R-language connection handle.
   *
   * @param context the Renjin execution context.
   * @param conn An R-language connection handle of type 'integer' and class 'connection'
   * @return the {@code Connection} implementation.
   */
  public static Connection getConnection(Context context, SEXP conn) {
    int connIndex = getConnectionIndex(conn);
    return context.getSession().getConnectionTable().getConnection(connIndex);
  }

  private static int getConnectionIndex(SEXP conn) {
    if(!conn.inherits("connection") || !(conn instanceof Vector) || conn.length() != 1) {
      throw new EvalException("'con' is not a connection");
    }
    return ((Vector)conn).getElementAsInt(0);
  }

  private static IntVector newConnection(final Context context, String open, Connection conn) throws IOException, FileSystemException {
    if(!Strings.isNullOrEmpty(open)) {
      conn.open(new OpenSpec(open));
    }
    return context.getSession().getConnectionTable().newConnection(conn);
  }

  @Internal
  public static boolean isIncomplete(@Current Context context, SEXP conn) throws IOException {

    Connection con = Connections.getConnection(context, conn);

    return con.isIncomplete();
  }
}
