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

package r.lang.primitive;

import r.io.DatafileReader;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.Environment;

import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

public class Connections {

  public static final String CLASSPATH_PREFIX = "classpath:";

  public static Connection gzfile(final String description, String open, String encoding, double compressionLevel) {
    return new ConnectionImpl(new InputStreamFactory() {
      @Override
      public InputStream openInputStream() throws IOException {
        return new GZIPInputStream(openInput(description));
      }
    });
  }

  public static SEXP unserializeFromConn(Connection conn, EnvExp rho) throws IOException {
    DatafileReader reader = new DatafileReader(rho, conn.getInputStream());
    return reader.readFile();
  }

  public static void close(Connection conn, String type /* Unused */ ) throws IOException {
    conn.close();
  }

  /**
   * Populates a target Environment with  
   * @param names
   * @param values
   * @param expr
   * @param eenv
   * @param targetEnvironment
   */
  public static void makeLazy(StringExp names, ListExp values, LangExp expr, EnvExp eenv, EnvExp targetEnvironment) {

    for(int i = 0; i < names.length(); i++) {
      // the name of the symbol
      SymbolExp name = new SymbolExp(names.get(i));

      // c(pos, length) of the serialized object
      SEXP value = values.get(i).evalToExp((EnvExp) eenv);

      // create a new call, replacing the first argument with the
      // provided arg
      PairListExp.Builder newArgs = PairListExp.newBuilder();
      newArgs.add(value);
      for(int j=1;j<expr.getArguments().length();++j) {
        newArgs.add(expr.<SEXP>getArgument(j));
      }
      LangExp newCall = new LangExp(expr.getFunction(), newArgs.build());
      targetEnvironment.setVariable(name, new PromiseExp(newCall, eenv));
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
  public static SEXP lazyLoadDBfetch(@Environment final EnvExp rho,
                                     IntExp key,
                                     String file,
                                     int compression,
                                     final SEXP restoreFunction) throws IOException, DataFormatException
  {
    byte buffer[] = readRawFromFile(file, key);

    if(compression == 1) {
      buffer = decompress1(buffer);
    } else if(compression > 1) {
      throw new UnsupportedOperationException("compressed==" + compression + " in lazyLoadDBfetch not yet implemented");
    }

    DatafileReader reader = new DatafileReader(rho, new ByteArrayInputStream(buffer), new DatafileReader.PersistentRestorer() {
      @Override
      public SEXP restore(SEXP values) {
        LangExp call = LangExp.newCall(restoreFunction);
        SEXP result = call.evalToExp(rho.getGlobalEnvironment());
        return result;
      }
    });

    SEXP exp = reader.readFile();
    if(exp instanceof PromiseExp) {
      exp = ((PromiseExp) exp).force().getExpression();
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

//    outlen = (uLong) uiSwap(*((unsigned int *) p));
//    buf = (Bytef *) R_alloc(outlen, sizeof(Bytef));
//    res = uncompress(buf, &outlen, (Bytef *)(p + 4), inlen - 4);
//    if(res != Z_OK) error("internal error %d in R_decompress1", res);
//    ans = allocVector(RAWSXP, outlen);
//    memcpy(RAW(ans), buf, outlen);
//    return ans;

    return result;
  }

  public static byte[] readRawFromFile(String file, IntExp key) throws IOException {
    if(key.length() != 2) {
      throw new EvalException("bad offset/length argument");
    }
    int offset = key.get(0);
    int length = key.get(1);

    byte buffer[] = new byte[length];

    DataInputStream in = new DataInputStream(openInput(file));
    in.skipBytes(offset);
    in.readFully(buffer);

    return buffer;
  }

  private static InputStream openInput(String description) throws FileNotFoundException {
    if(description.equals("stdin")) {
      return java.lang.System.in;
    } else if(description.startsWith(CLASSPATH_PREFIX)) {
      return Connections.class.getResourceAsStream(description.substring(CLASSPATH_PREFIX.length()));
    } else {
      return new FileInputStream(description);
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
    public void close() throws IOException {
      if(inputStream != null) {
        inputStream.close();
      }

    }
  }

}
