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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.AllowNA;
import r.lang.primitive.annotations.ArgumentList;
import r.lang.primitive.annotations.Primitive;
import r.lang.primitive.annotations.Recycle;
import r.lang.primitive.regex.RE;

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

  public static String ngettext(double n,
                                String singularMessage,
                                String pluralMessage,
                                String domain) {
    // stub implementation; no translation
    return n == 1 ? singularMessage : pluralMessage;
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

    RE re = new RE(pattern, ignoreCase, extended, perl, fixed, useBytes);
    return  re.subst(x, replacement, RE.REPLACE_FIRSTONLY | RE.REPLACE_BACKREFERENCES );
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

    RE re = new RE(pattern, ignoreCase, extended, perl, fixed, useBytes);
    return re.subst(x, replacement, RE.REPLACE_ALL | RE.REPLACE_BACKREFERENCES );
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

    RE re = new RE(split, false, extended, perl, fixed, useBytes);
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

    RE re = new RE(pattern,ignoreCase,extended, perl,fixed,useBytes);
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
          result.add(i);
        }
      }
      return result.build();
    }
  }
}
