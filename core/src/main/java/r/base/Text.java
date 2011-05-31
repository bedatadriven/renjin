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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import r.base.regex.ExtendedRE;
import r.base.regex.RE;
import r.base.regex.REFactory;
import r.jvmi.annotations.AllowNA;
import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.Primitive;
import r.jvmi.annotations.Recycle;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;

public class Text {

  private Text() {}

  public static StringVector paste(ListVector arguments, String separator, String collapse) {

    int resultLength = arguments.maxElementLength();

    if(collapse == null) {
      String results[] = new String[resultLength];
      for(int index=0; index!=resultLength; ++index) {
        results[index] = Joiner.on(separator).join(
            transform(arguments, new StringElementAt(index)));
      }
      return new StringVector( results );

    } else {
      StringBuilder result = new StringBuilder();
      for(int index=0; index!=resultLength; ++index) {
        if(index != 0) {
          result.append(collapse);
        }
        Joiner.on(separator).appendTo(result,
            transform(arguments, new StringElementAt(index)));
      }
      return new StringVector( result.toString() );
    }
  }

  @Primitive("file.path")
  public static StringVector filePath(ListVector components, String fileSeparator) {
    return paste(components, fileSeparator, null);
  }

  public static StringVector sprintf(@ArgumentList ListVector arguments) {
    StringVector formatVector = toStringVector( arguments.getElementAsSEXP(0), "fmt" );
    StringVector.Builder result = StringVector.newBuilder();

    if( arguments.minElementLength() > 0) {

      int maxLen = arguments.maxElementLength();
      Object formatArgs[] = new Object[ arguments.length() -1 ];

      for(int resultIndex=0; resultIndex != maxLen; ++resultIndex) {

        Formatter format = new Formatter(
            formatVector.getElementAsString( resultIndex % formatVector.length() ));

        for(int i=1;i!=arguments.length();++i) {
          AtomicVector formatArg = toAtomicVector(arguments.getElementAsSEXP(i));
          int formatArgIndex = resultIndex % formatArg.length();

          formatArgs[i-1] = formatArg.getElementAsObject(formatArgIndex);
        }

        result.add( format.sprintf(formatArgs) );
      }
    }

    return result.build();
  }

  private static StringVector toStringVector(SEXP argument, String argName) {
    if(argument instanceof StringVector) {
      return (StringVector) argument;
    } else {
      throw new EvalException("'%s' is not a character vector", argName);
    }
  }

  private static AtomicVector toAtomicVector(SEXP argument) {
    if(argument instanceof AtomicVector) {
      return (AtomicVector) argument;
    } else {
      throw new EvalException("unsupported type");
    }
  }

  private static class StringElementAt implements Function<SEXP, String> {
    private int index;

    private StringElementAt(int index) {
      this.index = index;
    }

    @Override
    public String apply(SEXP input) {
      if(input.length() == 0) {
        return "";
      } else if(input instanceof AtomicVector) {
        return ((AtomicVector) input).getElementAsString(index % input.length());

      } else if(input instanceof ListVector) {
        SEXP element = ((ListVector) input).getElementAsSEXP(index % input.length());
        return listElementToString(element);

      } else {
        throw new EvalException(String.format("Cannot coerce argument of type '%s' to character.",
            input.getTypeName()));
      }
    }

    private String listElementToString(SEXP element) {
      if(element.length() == 1 && element instanceof AtomicVector) {
        return ((AtomicVector) element).getElementAsString(0);
      } else {
        return Parse.deparse(element);
      }
    }
  }

  /**
   * Retrieve the translation for a natural language message
   * @param domain
   * @param messages
   * @return
   */
  public static StringVector gettext(String domain, StringVector messages) {
    // stub implementation; no translation
    return messages;
  }

  @Recycle(false)
  public static String ngettext(double n,
                                String singularMessage,
                                String pluralMessage,
                                String domain) {
    // stub implementation; no translation
    return n == 1 ? singularMessage : pluralMessage;
  }

  public static void bindtextdomain(String domain, String dirname) {
    // translation not yet supported.
  }

  /**
   * Translate characters
   *
   * @param oldChars a set of characters for which to search
   * @param newChars  a set of characters with which to replace matching characters
   * @param x the string in which to search
   * @return the translated string
   */
  @Primitive("chartr")
  public static String chartr(String oldChars, String newChars, @Recycle String x) {
    StringBuilder translation = new StringBuilder(x.length());
    for(int i=0;i!=x.length();++i) {
      int codePoint = x.codePointAt(i);
      int charIndex = oldChars.indexOf(codePoint);
      if(charIndex == -1) {
        translation.appendCodePoint(codePoint);
      } else {
        translation.appendCodePoint(newChars.codePointAt(charIndex));
      }
    }
    return translation.toString();
  }

  public static String tolower(String x) {
    return x.toLowerCase();
  }

  public static String toupper(String x) {
    return x.toUpperCase();
  }

  /**
   * String length
   * @param x
   * @param type
   * @param allowNA
   * @return
   */
  @AllowNA
  public static int nchar(@Recycle String x, String type, boolean allowNA) {
    if(StringVector.isNA(x)) {
      return 2;
    } else {
      return x.length();
    }
  }

  /**
   * Check for non-zero length string
   * @param x the string to check
   * @return true if the string of non-zero length, false if the string is empty
   */
  @AllowNA
  public static boolean nzchar(String x) {
    return StringVector.isNA(x) || x.length() != 0;
  }

  /**
   * Substitute the first pattern in a string
   * @param pattern a regular expression pattern to look for
   * @param replacement the string with which to replace matches. Can contain backreferences
   * denoted by \1, \2, ...\n
   * @param x The string in which to replace
   * @param ignoreCase  true to ignore case
   * @param extended true to use extended regexps
   * @param perl true to use perl-compatible regexps
   * @param fixed true to use normal string replacement
   * @param useBytes true to perform matching on byte-level rather than character-level.
   * Not supported
   * @return  the string with replacements made
   */
  public static String sub(String pattern, String replacement,
                           @Recycle String x,
                           boolean ignoreCase,
                           boolean extended,
                           boolean perl,
                           boolean fixed,
                           boolean useBytes) {

    RE re = REFactory.compile(pattern, ignoreCase, extended, perl, fixed, useBytes);
    return  re.subst(x, replacement, ExtendedRE.REPLACE_FIRSTONLY | ExtendedRE.REPLACE_BACKREFERENCES );
  }


  /**
   * Substitute the all patterns in a string
   * @param pattern a regular expression pattern to look for
   * @param replacement the string with which to replace matches. Can contain backreferences
   * denoted by \1, \2, ...\n
   * @param x The string in which to replace
   * @param ignoreCase  true to ignore case
   * @param extended true to use extended regexps
   * @param perl true to use perl-compatible regexps
   * @param fixed true to use normal string replacement
   * @param useBytes true to perform matching on byte-level rather than character-level.
   * Not supported
   * @return  the string with replacements made
   */
  public static String gsub(String pattern, String replacement,
                            @Recycle String x,
                            boolean ignoreCase,
                            boolean extended,
                            boolean perl,
                            boolean fixed,
                            boolean useBytes) {

    RE re = REFactory.compile(pattern, ignoreCase, extended, perl, fixed, useBytes);
    return re.subst(x, replacement, ExtendedRE.REPLACE_ALL | ExtendedRE.REPLACE_BACKREFERENCES );
  }

  /**
   * Substitute the all patterns in a string
   * @param split a regular expression pattern to look for
   * @param x The string in which to replace
   * @param extended true to use extended regexps
   * @param perl true to use perl-compatible regexps
   * @param fixed true to use normal string replacement
   * @param useBytes true to perform matching on byte-level rather than character-level.
   * Not supported
   * @return  a {@code StringVector} containing the splits
   */
  public static StringVector strsplit(@Recycle String x, @Recycle String split,
                                      boolean extended,
                                      boolean fixed,
                                      boolean perl,
                                      boolean useBytes) {

    RE re = REFactory.compile(split, false, extended, perl, fixed, useBytes);
    return new StringVector( re.split(x) );
  }

  public static Vector grep(
      String pattern,
      StringVector x,
      boolean ignoreCase,
      boolean extended,
      boolean value,
      boolean perl,
      boolean fixed,
      boolean useBytes,
      boolean invert) {

    RE re = REFactory.compile(pattern,ignoreCase,extended, perl, fixed, useBytes);
    if(value) {
      StringVector.Builder result = new StringVector.Builder();
      for(String string : x) {
        if(re.match(string)) {
          result.add(string);
        }
      }
      return result.build();
    } else {

      IntVector.Builder result = new IntVector.Builder(0);
      for(int i=0;i!=x.length();++i) {
        if(re.match(x.getElementAsString(i))) {
          result.add(i+1);
        }
      }
      return result.build();
    }
  }

  /**
   *
   * @param pattern
   * @param x
   * @param ignoreCase
   * @param extended
   * @param value
   * @param perl
   * @param fixed
   * @param useBytes
   * @param invert
   * @return a logical vector (match or not for each element of x).
   */
  public static Vector grepl(
      String pattern,
      StringVector x,
      boolean ignoreCase,
      boolean extended,
      boolean value,
      boolean perl,
      boolean fixed,
      boolean useBytes,
      boolean invert) {

    RE re = REFactory.compile(pattern, ignoreCase, extended, perl, fixed, useBytes);
    LogicalVector.Builder result = new LogicalVector.Builder();
    for(String string : x) {
      result.add( ! StringVector.isNA(string) && re.match(string ));
    }
    return result.build();
  }

  public static Vector agrep(String pattern, StringVector x, boolean ignoreCase, boolean value,
                              int maxDistance, int maxDeletions, int maxInsertions,
                              int maxSubstitutions, boolean useBytes) {

    if(value) {
      StringVector.Builder result = new StringVector.Builder();
      for(int i=0;i!=x.length();++i) {
        if(distance(pattern, x.getElementAsString(i)) < maxDistance) {
          result.add(x.getElementAsString(i));
        }
      }
      return result.build();
    } else {
      IntVector.Builder result = new IntVector.Builder();
      for(int i=0;i!=x.length();++i) {
        if(distance(pattern, x.getElementAsString(i)) < maxDistance) {
          result.add(i);
        }
      }
      return result.build();
    }
  }


  private static int minimum(int a, int b, int c)
  {
      int mi = a;
      if (b < mi)
          mi = b;
      if (c < mi)
          mi = c;
      return mi;
  }


  /**
   * Compute Levenshtein distance between two strings.
   * Source: org.apache.xmlbeans.impl.common
   * @param s
   * @param t
   * @return
   */
  private static int distance(String s, String t)
  {
      int d[][]; // matrix
      int n; // length of s
      int m; // length of t
      int i; // iterates through s
      int j; // iterates through t
      char s_i; // ith character of s
      char t_j; // jth character of t
      int cost; // cost

      // Step 1
      n = s.length();
      m = t.length();
      if (n == 0)
          return m;
      if (m == 0)
          return n;
      d = new int[n+1][m+1];

      // Step 2
      for (i = 0; i <= n; i++)
          d[i][0] = i;
      for (j = 0; j <= m; j++)
          d[0][j] = j;

      // Step 3
      for (i = 1; i <= n; i++)
      {
          s_i = s.charAt (i - 1);

          // Step 4
          for (j = 1; j <= m; j++)
          {
              t_j = t.charAt(j - 1);

              // Step 5
              if (s_i == t_j)
                  cost = 0;
              else
                  cost = 1;

              // Step 6
              d[i][j] = minimum(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
          }
      }

      // Step 7
      return d[n][m];
  }

  public static StringVector substr(StringVector x, int start, int stop) {
    StringVector.Builder result = new StringVector.Builder();
    for(String s : x) {
      if(start > s.length()) {
        result.add("");
      } else if(stop >= s.length()) {
        result.add(s.substring(start-1));
      } else {
        result.add(s.substring(start-1, stop));
      }
    }
    return result.build();
  }

  /**
   * Make syntactically valid names out of character vectors.
   *
   * A syntactically valid name consists of letters, numbers and the dot or underline
   * characters and starts with a letter or the dot not followed by a number. Names such as
   * ".2way" are not valid, and neither are the reserved words.
   *
   * The character "X" is prepended if necessary. All invalid characters are translated to ".".
   * A missing value is translated to "NA". Names which match R keywords have a dot appended to them.
   *
   * @param names
   * @param allow
   * @return
   */
  @Primitive("make.names")
  @AllowNA
  public static String makeNames(@Recycle String name, @Recycle(false) boolean allow) {
    if(StringVector.isNA(name)) {
      return "NA.";
    } else if(name.isEmpty() || !legalFirstCharacter(name)) {
      return "X" + replaceIllegalCharacters(name);
    } else if(ReservedWords.isReserved(name)) {
      return name + ".";
    } else {
      return replaceIllegalCharacters(name);
    }
  }

  private static boolean legalFirstCharacter(String name) {
    char first = name.charAt(0);
    return Character.isLetter(first) ||
        (first == '.' && !secondCharacterIsDigit(name));
  }

  private static boolean secondCharacterIsDigit(String name) {
    return name.length() >= 2 && Character.isDigit(name.codePointAt(1));
  }

  private static String replaceIllegalCharacters(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    for(int i=0;i!=name.length();++i) {
      int cp = name.codePointAt(i);
      if(cp == '_' || Character.isDigit(cp) || Character.isLetter(cp)) {
        sb.appendCodePoint(cp);
      } else {
        sb.append('.');
      }
    }
    return sb.toString();
  }

  /**
   * Makes the elements of a character vector unique by appending sequence numbers to duplicates.
   *
   * @param names  a character vector
   * @param sep a character string used to separate a duplicate name from its sequence number.
   * @return  A character vector of same length as names with duplicates changed,
   *         in the current locale's encoding.
   */
  @Primitive("make.unique")
  public static StringVector makeUnique(StringVector names, String sep) {
    Set<String> set = new HashSet<String>();
    StringVector.Builder result = new StringVector.Builder();

    for(String name : names) {
      String uniqueName = makeUnique(sep, set, name);
      result.add(uniqueName);
      set.add(uniqueName);
    }
    return result.build();
  }

  private static String makeUnique(String sep, Set<String> set, String name) {
    if(set.contains(name)) {
      int i=1;
      String newName;
      while(set.contains(newName=name+sep+i)) {
        i++;
      }
      return newName;
    } else {
      return name;
    }
  }

}
