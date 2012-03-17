/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program
import com.google.common.base.Strings;
 is free software: you can redistribute it and/or modify
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

package org.renjin.primitives;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.io.connections.Connection;
import org.renjin.primitives.io.connections.Connections;

import r.lang.Context;
import r.lang.ExternalExp;
import r.lang.ListVector;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class Scan {


  public static Vector scan(@Current Context context,
                            SEXP file,
                            Vector what,
                            int nmax,
                            String seperator,
                            String dec,
                            String quote,
                            int skip,
                            int nlines,
                            StringVector naStrings,
                            boolean flush,
                            boolean fill,
                            boolean stripWhite,
                            boolean quiet,
                            boolean blankLinesSkip,
                            boolean multiLine,
                            String commentChar,
                            boolean allowEscapes,
                            String encoding) throws IOException {
    InputStream in;
    if(file instanceof StringVector) {
      String fileName = ((StringVector) file).getElementAsString(0);
      if(fileName.length() == 0) {
        in = java.lang.System.in;
      } else {
        in = Connections.file(context,fileName,"o",true,encoding).getValue().getInputStream();
      }
    } else if(file instanceof ExternalExp && ((ExternalExp) file).getValue() instanceof Connection) {
        in = ((Connection) ((ExternalExp) file).getValue()).getInputStream();
    } else {
      throw new EvalException("illegal file argument");
    }

    BufferedReader lineReader = new BufferedReader( new InputStreamReader( in, toJavaEncoding(encoding)));

    Scanner scanner;
    if(what instanceof ListVector) {
      scanner = new ListReader((ListVector)what, seperator);
    } else {
      scanner = getAtomicScanner(what);
    }
    
    String line;
    while( (line=lineReader.readLine())!=null) {
      if(!Strings.isNullOrEmpty(commentChar)) {
        if(line.startsWith(commentChar)) {
          continue;
        }
      }
      scanner.read(line);
    }
    return scanner.build();
  }

  private static String toJavaEncoding(String encoding) {
    return "UTF-8";
  }
  
  interface Scanner {
    void read(String line);
    Vector build();
  }

  private static class StringReader implements Scanner {
    private final StringVector.Builder builder;

    private StringReader() {
      this.builder = new StringVector.Builder();
    }

    public void read(String value) {
      this.builder.add(value);
    }

    public StringVector build() {
      return builder.build();
    }
  }
  
  
  private static Scanner getAtomicScanner(SEXP exp) {
    if(exp instanceof StringVector) {
      return new StringReader();
    } else {
      throw new UnsupportedOperationException(
          String.format("column type '%s' not implemented", exp.getTypeName()));
    }
  }
  
  private static class ListReader implements Scanner {

    private String seperator;
    private StringVector names;
    private List<Scanner> columnReaders = Lists.newArrayList();
        
    public ListReader(ListVector columns, String sep) {
      this.seperator = sep;
      this.names = (StringVector) columns.getAttribute(Symbols.NAMES);
      for(SEXP column : columns) {
        columnReaders.add(getAtomicScanner(column));
      }
    }
    
    @Override
    public void read(String line) {
      String[] fields = line.split(seperator);
      for(int i=0;i!=fields.length;++i) {
        columnReaders.get(i).read(fields[i]);
      }
    }

    @Override
    public Vector build() {
      ListVector.Builder result = new ListVector.Builder();
      for(Scanner scanner : columnReaders) {
        result.add(scanner.build());
      }
      result.setAttribute(Symbols.NAMES, names);
      return result.build();
    }
    
  }
}
