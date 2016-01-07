package org.renjin.utils;


import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.parser.NumericLiterals;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.PushbackBufferedReader;
import org.renjin.sexp.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Tables {
  
  public static StringVector readtablehead(@Current Context context,
                                           SEXP conn, double nLines, String commentChar, boolean blankLinesSkip, 
                                           String quote, String sep, boolean skipNul) throws IOException {
    
    PushbackBufferedReader reader = Connections.getConnection(context, conn).getReader();
    
    StringVector.Builder head = new StringVector.Builder();
    String line;
    while( nLines > 0 && (line=reader.readLine())!=null) {
      head.add(line);
      nLines -- ;
    }
    return head.build();
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
  public static Vector typeconvert(StringVector vector, StringVector naStrings, boolean asIs, 
                                   String dec, String numerals) {

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
      for(Map.Entry<String, Integer> level : codes.entrySet()) {
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
    LogicalArrayVector.Builder newBuilder(int length) {
      return new LogicalArrayVector.Builder(length);
    } 
  }

  private static class IntConverter extends Converter<IntArrayVector.Builder> {
    @Override
    public boolean accept(String string) {
      try {
        double doubleValue = NumericLiterals.parseDouble(string);
        int intValue = (int)doubleValue;
        return ((double)intValue) == doubleValue;

      } catch(Exception e) {
        return false;
      }
    }
    
    @Override
    public void set(IntArrayVector.Builder builder, int index, String string) {
      builder.set(index, NumericLiterals.parseInt(string));
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
        return !DoubleVector.isNA(NumericLiterals.parseDouble(string));
      } catch(Exception e) {
        return false;
      }
    }
    @Override
    public void set(DoubleArrayVector.Builder builder, int index, String string) {
      builder.set(index, NumericLiterals.parseDouble(string));
    }
    @Override
    DoubleArrayVector.Builder newBuilder(int length) {
      return new DoubleArrayVector.Builder(length);
    }
  }
}

