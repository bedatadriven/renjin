/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.utils;


import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.parser.NumericLiterals;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.PushbackBufferedReader;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.*;

import java.io.IOException;
import java.util.Arrays;
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
      if(commentChar.isEmpty() || !line.startsWith(commentChar)) {
        head.add(line);
        nLines--;
      }
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
    Converter<?> converter = getConverter(vector, naSet, dec.charAt(0));
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
    String[] strings = vector.toStringArray();
    String[] unique = Arrays.stream(strings).distinct().toArray(String[]::new);
    Arrays.sort(unique);

    Map<String, Integer> codes = Maps.newHashMap();
    StringVector.Builder levels = StringVector.newBuilder();
    for(int i = 0; i < unique.length; i++) {
      codes.put(unique[i], i+1);
      levels.set(i, unique[i]);
    }

    IntArrayVector.Builder factor = new IntArrayVector.Builder(vector.length());
    for(int i=0;i!=vector.length();++i) {
      String element = vector.getElementAsString(i);
      if(!isNa(element, naStrings)) {
        factor.set(i, codes.get(element));
      }
    }

    factor.setAttribute(Symbols.CLASS, StringVector.valueOf("factor"));
    factor.setAttribute(Symbols.LEVELS, levels.build());
    return factor.build();
  }

  private static boolean isNa(String string, Set<String> naStrings) {
    return Strings.isNullOrEmpty(string) || naStrings.contains(string);
  }

  private static Converter<?> getConverter(StringVector vector, Set<String> naStrings, char decimal) {
    Converter<?> converters[] = new Converter<?>[] {
        new LogicalConverter(),
        new IntConverter(),
        new DoubleConverter(decimal)
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

    private final char decimal;

    public DoubleConverter(char decimal) {
      this.decimal = decimal;
    }

    @Override
    public boolean accept(String string) {
      try {
        return !DoubleVector.isNA(NumericLiterals.parseDouble(string, 0, string.length(), decimal, false));
      } catch(Exception e) {
        return false;
      }
    }
    @Override
    public void set(DoubleArrayVector.Builder builder, int index, String string) {
      builder.set(index, NumericLiterals.parseDouble(string, 0, string.length(), decimal, false));
    }
    @Override
    DoubleArrayVector.Builder newBuilder(int length) {
      return new DoubleArrayVector.Builder(length);
    }
  }
}

