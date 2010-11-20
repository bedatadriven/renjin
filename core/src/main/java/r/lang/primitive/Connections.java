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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

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

  public static void makeLazy(StringExp names, ListExp values, LangExp expr, EnvExp eenv, EnvExp aenv) {

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
      aenv.setVariable(name, new PromiseExp(newCall, eenv));


      
//      PROTECT(expr0 = duplicate(expr));
//      SETCAR(CDR(expr0), val);
//      defineVar(name, mkPROMISE(expr0, eenv), aenv);
    }
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
