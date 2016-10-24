/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.parser.NumericLiterals;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.PushbackBufferedReader;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.io.IOException;
import java.util.List;

public class Scan {


  @Internal
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
    
    
    PushbackBufferedReader lineReader;
    if(file instanceof StringVector) {
      String fileName = ((StringVector) file).getElementAsString(0);
      if(fileName.length() == 0) {
        lineReader = context.getSession().getConnectionTable().getStdin().getReader();
      } else {
        SEXP fileConn = Connections.file(context,fileName,"o",true,encoding,false);
        lineReader = Connections.getConnection(context, fileConn).getReader();
      }
    } else {
      lineReader = Connections.getConnection(context, file).getReader();
    }

    Splitter splitter;
    if(Strings.isNullOrEmpty(seperator)) {
      splitter = new WhitespaceSplitter(quote);
    } else {
      splitter = new CharSplitter(quote, seperator);
    }
    
    Scanner scanner;
    if(what instanceof ListVector) {
      scanner = new ListReader((ListVector)what, splitter, dec.charAt(0));
    } else {
      scanner = new ScalarReader(getAtomicScanner(what, dec.charAt(0)), splitter);
    }

    String line;
    int linesRead = 0;
    int linesSkipped = 0;
    while( (linesRead < nlines || nlines <= 0) &&
            (line=lineReader.readLine())!=null) {
      if (linesSkipped < skip) {
        linesSkipped++;
        continue;
      }
      linesRead ++;

      if(blankLinesSkip && line.isEmpty()) {
        continue;
      }
      if(!Strings.isNullOrEmpty(commentChar) && line.startsWith(commentChar)) {
        continue;
      }
      scanner.read(line);
    }
    return scanner.build();
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
  
  private static class DoubleReader implements Scanner {
    private final char decimal;
    private final DoubleArrayVector.Builder builder;

    private DoubleReader(char decimal) {
      this.decimal = decimal;
      this.builder = new DoubleArrayVector.Builder();
    }

    @Override
    public void read(String line) {
      builder.add( NumericLiterals.parseDouble(line, 0, line.length(), decimal, false) );
    }

    @Override
    public Vector build() {
      return builder.build();
    }
  }
  
  private static class IntReader implements Scanner {
    private final IntArrayVector.Builder builder = new IntArrayVector.Builder();

    @Override
    public void read(String line) {
      builder.add( NumericLiterals.parseInt(line));
    }

    @Override
    public Vector build() {
      return builder.build();
    }
  }
  
  
  
  private static Scanner getAtomicScanner(SEXP exp, char decimal) {
    if(exp instanceof StringVector) {
      return new StringReader();
    } else if(exp instanceof DoubleVector) {
      return new DoubleReader(decimal);
    } else if(exp instanceof IntVector) {
      return new IntReader();
    } else {
      throw new UnsupportedOperationException(
          String.format("column type '%s' not implemented", exp.getTypeName()));
    }
  }
  
  private static class ScalarReader implements Scanner {
    private Splitter splitter;
    private Scanner columnReader;
        
    public ScalarReader(Scanner scanner, Splitter splitter) {
      this.splitter = splitter;
      this.columnReader = scanner;
    }
    
    @Override
    public void read(String line) {
      List<String> fields = splitter.split(line);
      for(int i=0;i!=fields.size();++i) {
        columnReader.read(fields.get(i));
      }
    }

    @Override
    public Vector build() {
      return columnReader.build();
    }
  }
  
  private static class ListReader implements Scanner {

    private Splitter splitter;
    private StringVector names;
    private List<Scanner> columnReaders = Lists.newArrayList();
        
    public ListReader(ListVector columns, Splitter splitter, char decimal) {
      this.splitter = splitter;
      this.names = (StringVector) columns.getAttribute(Symbols.NAMES);
      for(SEXP column : columns) {
        columnReaders.add(getAtomicScanner(column, decimal));
      }
    }
    
    @Override
    public void read(String line) {
      List<String> fields = splitter.split(line);
      for(int i=0;i!=fields.size();++i) {
        columnReaders.get(i).read(fields.get(i));
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
  
  interface Splitter {
    List<String> split(String line);
  }
  
  static class CharSplitter implements Splitter {
    private char quote;
    private char separator;

    public CharSplitter(String quote, String separator) {
      this.quote = quote.charAt(0);
      this.separator = separator.charAt(0);
    }

    public List<String> split(String line) {
      StringBuilder sb = new StringBuilder();
      List<String> fields = Lists.newArrayList();
      boolean quoted = false;
      for (int i = 0; i != line.length(); ++i) {
        char c = line.charAt(i);
        if (c == quote) {
          quoted = !quoted;
        } else if (!quoted && c == separator) {
          fields.add(sb.toString());
          sb.setLength(0);
        } else {
          sb.append(c);
        }
      }
      fields.add(sb.toString());
      return fields;
    }
  }
  
  static class WhitespaceSplitter implements Splitter {
    private final char quote;

    public WhitespaceSplitter(String quote) {
      this.quote = quote.charAt(0);
    }

    @Override
    public List<String> split(String line) {
      StringBuilder sb = new StringBuilder();
      List<String> fields = Lists.newArrayList();
      boolean quoted = false;
      for (int i = 0; i != line.length(); ++i) {
        char c = line.charAt(i);
        if (c == quote) {
          quoted = !quoted;
        } else if (!quoted && Character.isWhitespace(c)) {
          if(sb.length() > 0) {
            fields.add(sb.toString());
            sb.setLength(0);
          }
        } else {
          sb.append(c);
        }
      }
      if(sb.length() > 0) {
        fields.add(sb.toString());
      }
      return fields;    
    }
  }


}
