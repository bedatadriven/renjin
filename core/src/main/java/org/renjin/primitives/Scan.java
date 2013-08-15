/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program
import org.renjin.sexp.LogicalArrayVector;

import org.renjin.sexp.LogicalArrayVector;

import org.renjin.sexp.LogicalArrayVector;

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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Internal;
import org.renjin.parser.ParseUtil;
import org.renjin.invoke.annotations.Current;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.PushbackBufferedReader;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalArrayVector.Builder;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Scan {


  @Internal
  public static StringVector readTableHead(@Current Context context,
     SEXP conn, int nLines, String commentChar, int blankLinesSkip, String quote, String sep) throws IOException {
    
    PushbackBufferedReader reader = Connections.getConnection(context, conn).getReader();
    
    StringVector.Builder head = new StringVector.Builder();
    String line;
    while( nLines > 0 && (line=reader.readLine())!=null) {
      head.add(line);
      nLines -- ;
    }
    return head.build();
  }

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
      scanner = new ListReader((ListVector)what, splitter);
    } else {
      scanner = new ScalarReader(getAtomicScanner(what), splitter);
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
      if(!Strings.isNullOrEmpty(commentChar)) {
        if(line.startsWith(commentChar)) {
          continue;
        }
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
    private final DoubleArrayVector.Builder builder;
    
    private DoubleReader() {
      this.builder = new DoubleArrayVector.Builder();
    }

    @Override
    public void read(String line) {
      builder.add( ParseUtil.parseDouble(line) );
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
      builder.add( ParseUtil.parseInt( line ));
    }

    @Override
    public Vector build() {
      return builder.build();
    }
  }
  
  
  
  private static Scanner getAtomicScanner(SEXP exp) {
    if(exp instanceof StringVector) {
      return new StringReader();
    } else if(exp instanceof DoubleVector) {
      return new DoubleReader();
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
        
    public ListReader(ListVector columns, Splitter splitter) {
      this.splitter = splitter;
      this.names = (StringVector) columns.getAttribute(Symbols.NAMES);
      for(SEXP column : columns) {
        columnReaders.add(getAtomicScanner(column));
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
  
  private static abstract class Splitter {
    private char quote;
    
    public Splitter(char quote) {
      this.quote = quote;
    }

    public List<String> split(String line) {
      StringBuilder sb = new StringBuilder();
      List<String> fields = Lists.newArrayList();
      boolean quoted = false;
      for(int i=0;i!=line.length();++i) {
        char c = line.charAt(i);
        if(c == quote) {
          quoted = !quoted;
        } else if(!quoted && isSeperator(c)) {
          fields.add(sb.toString());
          sb.setLength(0);
        } else {
          sb.append(c);
        }
      }
      fields.add(sb.toString());
      return fields;
    }

    protected abstract boolean isSeperator(char c);
  }
  
  private static class WhitespaceSplitter extends Splitter {

    public WhitespaceSplitter(String quote) {
      super(quote.charAt(0));
    }

    @Override
    protected boolean isSeperator(char c) {
      return Character.isWhitespace(c);
    } 
  }
  
  private static class CharSplitter extends Splitter {
    private char sep;

    public CharSplitter(String quote, String seperator) {
      super(quote.charAt(0));
      this.sep = seperator.charAt(0);
    }

    @Override
    protected boolean isSeperator(char c) {
      return c == sep;
    }
  }
  
  
  /**
   * This is principally a helper function for ‘read.table’.  Given a
     character vector, it attempts to convert it to logical, integer,
     numeric or complex, and failing that converts it to factor unless
     ‘as.is = TRUE’.  The first type that can accept all the
     non-missing values is chosen.

     Vectors which are entirely missing values are converted to
     logical, since ‘NA’ is primarily logical.

     Vectors containing ‘F’, ‘T’, ‘FALSE’, ‘TRUE’ or values from
     ‘na.strings’ are converted to logical.  Vectors containing
     optional whitespace followed by decimal constants representable as
     R integers or values from ‘na.strings’ are converted to integer.
     Other vectors containing optional whitespace followed by other
     decimal or hexadecimal constants (see NumericConstants), or ‘NaN’,
     ‘Inf’ or ‘infinity’ (ignoring case) or values from ‘na.strings’
     are converted to numeric.

   * @param vector
   * @param naStrings
   * @param asIs
   * @param dec
   * @return
   */
  @Internal("type.convert")
  public static Vector typeConvert(StringVector vector, StringVector naStrings, boolean asIs, String dec) {

    Set<String> naSet = createHashSet(naStrings);
    Converter<?> converter = getConverter(vector, naSet);
    if(converter != null) {
      return converter.build(vector, naSet);
    } else if(asIs) {
      return vector;
    } else {
      return buildFactor(vector, naSet);
    }
  }

  private static Set<String> createHashSet(StringVector strings) {
    java.util.HashSet<String> set = Sets.newHashSet();

    for (int i = 0; i < strings.length(); i++)  {
      String element = strings.getElementAsString(i);
      if (!Strings.isNullOrEmpty(element)) {
        set.add(element);
      }
    }

    return set;
  }

  private static Vector buildFactor(StringVector vector, Set<String> naStrings) {
      Map<String, Integer> codes = Maps.newHashMap();
      IntArrayVector.Builder factor = new IntArrayVector.Builder(vector.length());
      for(int i=0;i!=vector.length();++i) {
        String element = vector.getElementAsString(i);
        if(!isNa(element, naStrings)) {
          Integer code = codes.get(element);
          if(code == null) {
            code = codes.size()+1;
            codes.put(element, code);
          }
          factor.set(i, code);
        }
      }
      StringVector.Builder levels = StringVector.newBuilder();
      for(Entry<String, Integer> level : codes.entrySet()) {
        levels.set(level.getValue()-1, level.getKey());
      }
      factor.setAttribute(Symbols.CLASS, StringVector.valueOf("factor"));
      factor.setAttribute(Symbols.LEVELS, levels.build());
      return factor.build();
  }

  private static boolean isNa(String string, Set<String> naStrings) {
    return Strings.isNullOrEmpty(string) || naStrings.contains(string);
  }

  private static Converter<?> getConverter(StringVector vector, Set<String> naStrings) {
    Converter<?> converters[] = new Converter<?>[] {
        new LogicalConverter(),
        new IntConverter(),
        new DoubleConverter()
    };
    for(Converter<?> converter : converters) {
      if(converter.accept(vector, naStrings)) {
        return converter;
      }
    }
    return null;
  }
  
  
  private static abstract class Converter<BuilderT extends Vector.Builder> {
    abstract boolean accept(String string);
    
    abstract BuilderT newBuilder(int length);
    abstract void set(BuilderT builder, int index, String string);
    
    public boolean accept(StringVector vector, Set<String> naStrings) {
      for(int i=0;i!=vector.length();++i) {
        String element = vector.getElementAsString(i);
        if(!isNa(element, naStrings) && !accept(element)) {
          return false;
        }
      }
      return true;
    }
    
    public Vector build(StringVector vector, Set<String> naStrings) {
      BuilderT builder = newBuilder(vector.length());
      for(int i=0;i!=vector.length();++i) {
        String element = vector.getElementAsString(i);
        if(!isNa(element, naStrings)) {
          set(builder, i, element);
        }
      }
      return builder.build();
    }
    
  }
  
  private static class LogicalConverter extends Converter<LogicalArrayVector.Builder> {
    @Override
    public boolean accept(String string) {
      return string.equals("T") || string.equals("F") || string.equals("TRUE") || string.equals("FALSE");
    }
    @Override
    public void set(LogicalArrayVector.Builder builder, int index, String string) {
      if(string.equals("T") || string.equals("TRUE")) {
        builder.set(index, true);
      } else {
        builder.set(index, false);
      }
    }
    @Override
    Builder newBuilder(int length) {
      return new LogicalArrayVector.Builder(length);
    } 
  }
  
  private static class IntConverter extends Converter<IntArrayVector.Builder> {
    @Override
    public boolean accept(String string) {
      try {
        ParseUtil.parseInt(string);
        return true;
      } catch(Exception e) {
        return false;
      }
    }
    
    @Override
    public void set(IntArrayVector.Builder builder, int index, String string) {
      builder.set(index, ParseUtil.parseInt(string));
    }

    @Override
    public IntArrayVector.Builder newBuilder(int length) {
      return new IntArrayVector.Builder(length);
    }
  } 
  
  private static class DoubleConverter extends Converter<DoubleArrayVector.Builder> {
    
    @Override
    public boolean accept(String string) {
      try {
        return !DoubleVector.isNA(ParseUtil.parseDouble(string));
      } catch(Exception e) {
        return false;
      }
    }
    @Override
    public void set(DoubleArrayVector.Builder builder, int index, String string) {
      builder.set(index, ParseUtil.parseDouble(string));
    }
    @Override
    DoubleArrayVector.Builder newBuilder(int length) {
      return new DoubleArrayVector.Builder(length);
    }
  } 
}
