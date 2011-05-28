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

package r.base;

import org.apache.commons.vfs.FileSystemException;
import r.base.connections.GzFileConnection;
import r.base.connections.OutputStreamConnection;
import r.base.connections.StdInConnection;
import r.io.DatafileReader;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Recycle;
import r.lang.*;
import r.lang.exception.EvalException;

import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 *
 *  Functions which create and manipulates connection objects.
 *
 * <p>
 * Connection objects are {@link ExternalExp}s that hold a reference to a
 * class implementing the {@link Connection} interface.
 *
 */
public class Connections {


  /**
   * Opens a connection to a gzipped file.
   *
   * @param context the current call Context
   * @param path path to the gzipped file
   * @param open the mode flag
   * @param encoding the character encoding if the file is to be opened for text reading
   * @param compressionLevel  integer 0-9
   * @return an external reference object which inherits from the (S3) class "connection"
   * @throws FileSystemException
   */
  public static ExternalExp<Connection> gzfile(@Current final Context context,
                                               final String path,
                                               String open,
                                               String encoding,
                                               double compressionLevel) throws FileSystemException {

    return new ExternalExp(new GzFileConnection(context.resolveFile(path)), "connection");
  }


  /**
   * Opens a connection to a file.

   * @param context the current Context
   * @param path path to the file
   * @param open the mode flag that determines how the file
   * @param blocking In blocking mode, functions using the connection do not return to the R evaluator
   *  until the read/write is complete. In non-blocking mode, operations return as soon as possible,
   * so on input they will return with whatever input is available (possibly none) and for output
   * they will return whether or not the write succeeded.
   *
   * @param encoding the character to encoding, if the file is to be opened as text
   * @return  an external reference object which inherits from the (S3) class "connection"
   */
  public static ExternalExp<Connection> file(@Current final Context context,
                                             final String path,
                                             String open,
                                             boolean blocking,
                                             String encoding) {
    Connection connection = new ConnectionImpl(new InputStreamFactory() {
      @Override
      public InputStream openInputStream() throws IOException {
        return openInput(context, path);
      }
    });

    return new ExternalExp(connection, "connection");
  }

  public static ExternalExp<Connection> stdin(@Current final Context context) {
    return new ExternalExp(new StdInConnection(), "connection");
  }

  public static ExternalExp<Connection> stdout(@Current final Context context) {
    return new ExternalExp(new OutputStreamConnection(java.lang.System.out), "connection");
  }

  public static ExternalExp<Connection> stderr() {
    return new ExternalExp<Connection>(new OutputStreamConnection(java.lang.System.err), "connection");
  }

  public static void cat(ListVector list, Connection connection, String sep, boolean fill, SEXP labels, boolean append) throws IOException {
    PrintWriter pw = connection.getPrintWriter();
    for(SEXP element : list) {
      pw.print(element.toString());
    }
  }

  public static SEXP unserializeFromConn(@Current Context context, Connection conn, Environment rho) throws IOException {
    DatafileReader reader = new DatafileReader(context, rho, conn.getInputStream());
    SEXP result =  reader.readFile();
    return result;
  }

  public static SEXP unserializeFromConn(@Current Context context, @Current Environment rho, Connection conn, Null nz) throws IOException {
    DatafileReader reader = new DatafileReader(context, rho, conn.getInputStream());
    SEXP result =  reader.readFile();
    return result;
  }

  /**
   * Reload datasets written with the function save.
   *
   * @param context
   * @param conn a (readable binary) connection or a character string giving the name of the file to load.
   * @param env the environment where the data should be loaded.
   * @return  A character vector of the names of objects created, invisibly.
   * @throws IOException
   */
  public static SEXP loadFromConn2(@Current Context context, Connection conn, Environment env) throws IOException {
    DatafileReader reader = new DatafileReader(context, env, conn.getInputStream());
    HasNamedValues data = EvalException.checkedCast(reader.readFile());

    StringVector.Builder names = new StringVector.Builder();

    for(NamedValue pair : data.namedValues()) {
      env.setVariable(new Symbol(pair.getName()), pair.getValue());
      names.add(pair.getName());
    }

    return names.build();
  }

  public static void close(Connection conn, String type /* Unused */ ) throws IOException {
    conn.close();
  }

  public static String readChar(Connection conn, int nchars, @Recycle(false) boolean useBytes) throws IOException {
    // at the moment, we assume the encoding is ASCII.
    // We can't easily use an InputStreamReader here because it uses a buffer
    // which messes up subsequent calls to read() on the underlying input stream

    InputStream in = conn.getInputStream();

    byte buffer[] = new byte[nchars];
    int bytesRead;
    int totalBytesRead =0;
    while(totalBytesRead < nchars &&
        (bytesRead=in.read(buffer, totalBytesRead, nchars-totalBytesRead))!=-1) {

      totalBytesRead += bytesRead;
    }

    return new String(buffer, "ASCII");
  }

  /**
   * Populates a target {@code Environment} with promises to serialized expressions.
   *
   * @param names the names of the symbols to be populated
   * @param values
   * @param expr
   * @param eenv
   * @param targetEnvironment
   */
  public static void makeLazy(@Current Context context, StringVector names, ListVector values, FunctionCall expr, Environment eenv, Environment targetEnvironment) {

    for(int i = 0; i < names.length(); i++) {
      // the name of the symbol
      Symbol name = new Symbol(names.getElement(i));

      // c(pos, length) of the serialized object
      SEXP value = values.get(i).evalToExp(context, (Environment) eenv);

      // create a new call, replacing the first argument with the
      // provided arg
      PairList.Node.Builder newArgs = PairList.Node.newBuilder();
      newArgs.add(value);
      for(int j=1;j<expr.getArguments().length();++j) {
        newArgs.add(expr.<SEXP>getArgument(j));
      }
      FunctionCall newCall = new FunctionCall(expr.getFunction(), newArgs.build());
      targetEnvironment.setVariable(name, new Promise(context, eenv, newCall));
    }
  }

  /**
   *  Retrieves a sequence of bytes as specified by a position/length key
   *  from a file, optionally decompresses, and unserializes the bytes.
   *  If the result is a promise, then the promise is forced.
   * @param key c(offset, length)
   * @param file the path to the file from which to load the value
   * @param compression 0=not compressed, 1=deflate, ...
   * @param restoreFunction a function called to load persisted objects from the serialized stream
   */
  public static SEXP lazyLoadDBfetch(@Current final Context context,
                                     @Current final Environment rho,
                                     IntVector key,
                                     String file,
                                     int compression,
                                     final SEXP restoreFunction) throws IOException, DataFormatException
  {
    byte buffer[] = readRawFromFile(context, file, key);

    if(compression == 1) {
      buffer = decompress1(buffer);
    } else if(compression > 1) {
      throw new UnsupportedOperationException("compressed==" + compression + " in lazyLoadDBfetch not yet implemented");
    }

    DatafileReader reader = new DatafileReader(context, rho, new ByteArrayInputStream(buffer), new DatafileReader.PersistentRestorer() {
      @Override
      public SEXP restore(SEXP values) {
        FunctionCall call = FunctionCall.newCall(restoreFunction, values);
        return call.evalToExp(context, rho.getGlobalEnvironment());
      }
    });

    SEXP exp = reader.readFile();
    if(exp instanceof Promise) {
      exp = ((Promise) exp).force().getExpression();
    }
    return exp;
  }


  public static byte[] decompress1(byte buffer[]) throws IOException, DataFormatException {
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));
    int outLength = in.readInt();

    Inflater inflater = new Inflater();
    inflater.setInput(buffer, 4, buffer.length-4);

    byte[] result = new byte[outLength];
    int resultLength = inflater.inflate(result);
    inflater.end();

    return result;
  }

  public static byte[] readRawFromFile(@Current Context context, String file, IntVector key) throws IOException {
    if(key.length() != 2) {
      throw new EvalException("bad offset/length argument");
    }
    int offset = key.getElementAsInt(0);
    int length = key.getElementAsInt(1);

    byte buffer[] = new byte[length];

    DataInputStream in = new DataInputStream(openInput(context, file));
    in.skipBytes(offset);
    in.readFully(buffer);

    return buffer;
  }

  private static InputStream openInput(Context context, String description) throws IOException {
    if(description.equals("stdin")) {
      return java.lang.System.in;
    } else {
      return context.resolveFile(description).getContent().getInputStream();
    }
  }

  private interface InputStreamFactory {
    InputStream openInputStream() throws IOException;
  }

  private static class ConnectionImpl implements Connection {
    private InputStream inputStream;
    private InputStreamFactory inputStreamFactory;

    private ConnectionImpl(InputStreamFactory inputStreamFactory) {
      this.inputStreamFactory = inputStreamFactory;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      if(inputStream == null) {
        inputStream = inputStreamFactory.openInputStream();
      }
      return inputStream;
    }

    @Override
    public PrintWriter getPrintWriter() throws IOException {
      throw new EvalException("cannot write to this connection");
    }

    @Override
    public void close() throws IOException {
      if(inputStream != null) {
        inputStream.close();
      }
    }
  }

}
