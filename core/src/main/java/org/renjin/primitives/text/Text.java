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

package org.renjin.primitives.text;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.primitives.Deparse;
import org.renjin.primitives.text.regex.ExtendedRE;
import org.renjin.primitives.text.regex.RE;
import org.renjin.primitives.text.regex.REFactory;
import org.renjin.sexp.*;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;

public class Text {

  private Text() {}

  @Internal
  public static StringVector paste(ListVector arguments, String separator, String collapse) {

    int resultLength = arguments.maxElementLength();

    if(collapse == null) {
      String results[] = new String[resultLength];
      for(int index=0; index!=resultLength; ++index) {
        results[index] = Joiner.on(separator).join(
            transform(arguments, new StringElementAt(index)));
      }
      return new StringArrayVector( results );

    } else {
      StringBuilder result = new StringBuilder();
      for(int index=0; index!=resultLength; ++index) {
        if(index != 0) {
          result.append(collapse);
        }
        Joiner.on(separator).appendTo(result,
            transform(arguments, new StringElementAt(index)));
      }
      return StringVector.valueOf(result.toString());
    }
  }

  @Internal("encodeString")
  public static StringVector encodeString(StringVector x, int width, String quote, 
      int justify, boolean naEncode) {
    
    return x;
  }
  
  @Internal("file.path")
  public static StringVector filePath(ListVector components, String fileSeparator) {
    return paste(components, fileSeparator, null);
  }

  @Internal
  @Materialize
  public static StringVector sprintf(@Current Context context, @Current Environment rho, 
        StringVector format, @ArgumentList ListVector arguments) {
    
    if(format.length() == 0) {
      return StringVector.EMPTY;
    }
    
    StringVector.Builder result = StringVector.newBuilder();

    Formatter[] formatters = new Formatter[format.length()];
    for(int i=0;i!=format.length();++i) {
      formatters[i] = new Formatter(format.getElementAsString(i));
    }

    // this is very tricky, but following the original R implementation, it seems
    // that even multiple format strings arguments are coerced only 
    
    AtomicVector[] formatArgs = new AtomicVector[arguments.length()];
    for(int i=0;i!=formatArgs.length;++i) {
      SEXP argument = arguments.getElementAsSEXP(i);
      if(formatters[0].isFormattedString(i) && !(argument instanceof StringVector)) {
        argument = context.evaluate( FunctionCall.newCall(Symbol.get("as.character"), argument), 
            rho); 
      }
      if(!(argument instanceof AtomicVector)) {
        throw new EvalException("Format argument %d is not an atomic vector", i);
      }
      formatArgs[i] = (AtomicVector)argument;
    }
    
    // count cycles
    int cycles = formatters.length;
    for (AtomicVector formatArg : formatArgs) {
      if (formatArg.length() == 0) {
        return StringVector.EMPTY;
      }
      if (formatArg.length() > cycles) {
        cycles = formatArg.length();
      }
    }
    

    for(int resultIndex=0; resultIndex != cycles; ++resultIndex) {

      Formatter formatter = formatters[resultIndex % formatters.length];
      
      result.add( formatter.sprintf(formatArgs, resultIndex) );
    }

    return result.build();
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
        AtomicVector vector = (AtomicVector) input;
        if(vector.isElementNA(index % input.length())) {
          return "NA";
        } else {
          return vector.getElementAsString(index % input.length());
        }

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
        return Deparse.deparseExp(null, element);
      }
    }
  }

  /**
   * Retrieve the translation for a natural language message
   * @param domain
   * @param messages
   * @return
   */
  @Internal
  public static StringVector gettext(String domain, StringVector messages) {
    // stub implementation; no translation
    return messages;
  }

  @Internal
  public static String ngettext(double n,
                                String singularMessage,
                                String pluralMessage,
                                String domain) {
    // stub implementation; no translation
    return n == 1 ? singularMessage : pluralMessage;
  }

  @Internal
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
  @Internal("chartr")
  @DataParallel
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

  @Internal
  @DataParallel
  public static String tolower(String x) {
    return x.toLowerCase();
  }

  @Internal
  @DataParallel
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
  @Internal
  @DataParallel(passNA = true)
  public static int nchar(@Recycle String x, String type, boolean allowNA) {
    if(StringVector.isNA(x)) {
      return 2;
    } else {
      return x.length();
    }
  }

  /**
   * Substitute the first pattern in a string
   * @param pattern a regular expression pattern to look for
   * @param replacement the string with which to replace matches. Can contain backreferences
   * denoted by \1, \2, ...\n
   * @param x The string in which to replace
   * @param ignoreCase  true to ignore case
   * @param perl true to use perl-compatible regexps
   * @param fixed true to use normal string replacement
   * @param useBytes true to perform matching on byte-level rather than character-level.
   * Not supported
   * @return  the string with replacements made
   */
  @Internal
  @DataParallel
  public static String sub(String pattern, String replacement,
                           @Recycle String x,
                           boolean ignoreCase,
                           boolean perl,
                           boolean fixed,
                           boolean useBytes) {
    
    RE re = REFactory.compile(pattern, ignoreCase, perl, fixed, useBytes);
    return  re.subst(x, replacement, ExtendedRE.REPLACE_FIRSTONLY | ExtendedRE.REPLACE_BACKREFERENCES );
  }


  /**
   * Substitute the all patterns in a string
   * @param pattern a regular expression pattern to look for
   * @param replacement the string with which to replace matches. Can contain backreferences
   * denoted by \1, \2, ...\n
   * @param x The string in which to replace
   * @param ignoreCase  true to ignore case
   * @param perl true to use perl-compatible regexps
   * @param fixed true to use normal string replacement
   * @param useBytes true to perform matching on byte-level rather than character-level.
   * Not supported
   * @return  the string with replacements made
   */
  @Internal
  @DataParallel
  public static String gsub(String pattern, String replacement,
                            @Recycle String x,
                            boolean ignoreCase,
                            boolean perl,
                            boolean fixed,
                            boolean useBytes) {

    RE re = REFactory.compile(pattern, ignoreCase, perl, fixed, useBytes);
    return re.subst(x, replacement, ExtendedRE.REPLACE_ALL | ExtendedRE.REPLACE_BACKREFERENCES );
  }

  /**
   * Substitute the all patterns in a string
   * @param split a regular expression pattern to look for
   * @param x The string in which to replace
   * @param perl true to use perl-compatible regexps
   * @param fixed true to use normal string replacement
   * @param useBytes true to perform matching on byte-level rather than character-level.
   * Not supported
   * @return  a {@code StringVector} containing the splits
   */
  @Internal
  @DataParallel
  public static StringVector strsplit(@Recycle String x, @Recycle String split,
                                      boolean fixed,
                                      boolean perl,
                                      boolean useBytes) {

    RE re = REFactory.compile(split, false, perl, fixed, useBytes);
    return new StringArrayVector( re.split(x) );
  }

  @Internal
  public static Vector grep(
      String pattern,
      StringVector x,
      boolean ignoreCase,
      boolean value,
      boolean perl,
      boolean fixed,
      boolean useBytes,
      boolean invert) {

    RE re = REFactory.compile(pattern,ignoreCase, perl, fixed, useBytes);
    if(value) {
      StringVector.Builder result = new StringVector.Builder();
      for(String string : x) {
        if(re.match(string)) {
          result.add(string);
        }
      }
      return result.build();
    } else {

      IntArrayVector.Builder result = new IntArrayVector.Builder(0);
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
   * @param value
   * @param perl
   * @param fixed
   * @param useBytes
   * @param invert
   * @return a logical vector (match or not for each element of x).
   */
  @Internal
  public static Vector grepl(
      String pattern,
      StringVector x,
      boolean ignoreCase,
      boolean value,
      boolean perl,
      boolean fixed,
      boolean useBytes,
      boolean invert) {

    RE re = REFactory.compile(pattern, ignoreCase,  perl, fixed, useBytes);
    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder();
    for(String string : x) {
      result.add( ! StringVector.isNA(string) && re.match(string ));
    }
    return result.build();
  }
  
  @Internal
  public static Vector agrep(String pattern, StringVector x,  boolean ignoreCase, boolean value,
                              Vector costs, Vector bounds, boolean useBytes, boolean fixed) {

    if(!fixed) {
      throw new EvalException("fixed = FALSE not impelmented for agrep.");
    }
  
    int maxDistance = maxDistance(bounds, pattern);
    
    FuzzyMatcher matcher = new FuzzyMatcher(pattern, ignoreCase);
    
    if(value) {
      StringVector.Builder result = new StringVector.Builder();
      for(int i=0;i!=x.length();++i) {
        if(matcher.contains(x.getElementAsString(i)) <= maxDistance) {
          result.add(x.getElementAsString(i));
        }
      }
      return result.build();
    } else {
      IntArrayVector.Builder result = new IntArrayVector.Builder();
      for(int i=0;i!=x.length();++i) {
        if(matcher.contains(x.getElementAsString(i)) <= maxDistance) {
          result.add(i+1);
        }
      }
      return result.build();
    }
  }
  
  private static int maxDistance(Vector bounds, String pattern) {

    if(bounds.length() != 5) {
      throw new EvalException("Expected bounds argument of length 5");
    }
    if (!bounds.isElementNA(1) || !bounds.isElementNA(2) || !bounds.isElementNA(3) ||
        !bounds.isElementNA(4)) {
      throw new EvalException("max distance with specific components (all, insertions, deletions, substitutions not implemented");
    }
    double maxDistance = bounds.getElementAsDouble(0);
    if(maxDistance < 1) {
      maxDistance = maxDistance * pattern.length();
    }
    return (int)Math.ceil(maxDistance);
  }


  @Internal
  public static IntVector regexpr(String pattern, StringVector vector, boolean ignoreCase, boolean perl,
      boolean fixed, boolean useBytes) {
    
    RE re = REFactory.compile(pattern, ignoreCase,  perl, fixed, useBytes);
    IntArrayVector.Builder position = IntArrayVector.Builder.withInitialCapacity(vector.length());
    IntArrayVector.Builder matchLength = IntArrayVector.Builder.withInitialCapacity(vector.length());
    
    for(String text : vector) {
      if(re.match(text)) {
        int start = re.getGroupStart(0);
        int end = re.getGroupEnd(0);
        position.add(start+1);
        matchLength.add(end-start);
      } else {
        position.add(-1);
        matchLength.add(-1);
      }
    }
    
    position.setAttribute("match.length", matchLength.build());
    position.setAttribute("useBytes", new LogicalArrayVector(useBytes));
    return position.build();
  }

  @Internal
  public static StringVector substr(StringVector x, Vector start, Vector stop) {
    int len = x.length();
    if ( len == 0 ) {
      return StringVector.EMPTY;
    }
    StringVector.Builder result = new StringVector.Builder();
    int k = start.length();
    int l = stop.length();
    if ( k == 0 || l == 0 ) {
      throw new EvalException("invalid substring arguments");
    }
    for (int i = 0; i < len; i++) {
      int startIndex = start.getElementAsInt(i % k);
      int stopIndex = stop.getElementAsInt(i % l);
      String element = x.getElementAsString(i);

      if(IntVector.isNA(startIndex) || IntVector.isNA(stopIndex) || StringVector.isNA(element)) { 
        result.add(StringVector.NA);
      } else {
        int slen = element.length();
        if (startIndex < 1) {
          startIndex = 1;
        }
        if (startIndex > stopIndex || startIndex > slen) {
          result.add("");
        } else if (stopIndex >= slen) {
          result.add(element.substring(startIndex - 1));
        } else {
          result.add(element.substring(startIndex - 1, stopIndex));
        }
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
   * @param name
   * @param allow
   * @return
   */
  @Internal("make.names")
  @DataParallel(passNA = true)
  public static String makeNames(@Recycle String name, @Recycle(false) boolean allow) {
    if(StringVector.isNA(name)) {
      return "NA.";
    } else if(name.isEmpty() || !Symbols.legalFirstCharacter(name)) {
      return "X" + replaceIllegalCharacters(name);
    } else if(ReservedWords.isReserved(name)) {
      return name + ".";
    } else {
      return replaceIllegalCharacters(name);
    }
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
  @Internal("make.unique")
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


  @Internal
  @DataParallel
  public static String strtrim(String source, int n) {
    int index;
    if (n > source.length()) {
      index = source.length();
    } else {
      index = n;
    }
    return (source.substring(0, index));
  }
    
  /**
   * 
   * @param x any R object (conceptually); typically numeric.

   * @param trim f ‘FALSE’, logical, numeric and complex values are
          right-justified to a common width: if ‘TRUE’ the leading
          blanks for justification are suppressed.
   * @param digits how many significant digits are to be used for numeric and
          complex ‘x’.  The default, ‘NULL’, uses ‘getOption(digits)’.
          This is a suggestion: enough decimal places will be used so
          that the smallest (in magnitude) number has this many
          significant digits, and also to satisfy ‘nsmall’.  (For the
          interpretation for complex numbers see ‘signif’.)

   * @param nsmall the minimum number of digits to the right of the decimal
          point in formatting real/complex numbers in non-scientific
          formats.  Allowed values are ‘0 <= nsmall <= 20’.

   * @param minWidth the _minimum_ field width or ‘NULL’ or ‘0’
          for no restriction.

   * @param zz
   * @param naEncode  should ‘NA’ strings be encoded?  Note this only
          applies to elements of character vectors, not to numerical or
          logical ‘NA’s, which are always encoded as ‘"NA"’.

   * @param scientific  Either a logical specifying whether elements of a real or
          complex vector should be encoded in scientific format, or an
          integer penalty (see ‘options("scipen")’).  Missing values
          correspond to the current default penalty.

   * @return
   */
  @Internal
  public static StringVector format(StringVector x, boolean trim, SEXP digits, SEXP nsmall,
      SEXP minWidth, int zz, boolean naEncode, SEXP scientific ) {
       
    List<String> elements = formatCharacterElements(x, naEncode);
    int width = calculateWidth(elements, minWidth);
    elements = justify(elements, width, Justification.LEFT, naEncode);
    
    return buildFormatResult(x, elements);
  }

  @Internal
  public static StringVector format(LogicalVector x, boolean trim, SEXP digits, SEXP nsmall,
      SEXP minWidth, int zz, boolean naEncode, SEXP scientific ) {
       
    List<String> elements = formatLogicalElements(x);
    int width = calculateWidth(elements, minWidth);
    elements = justify(elements, width, Justification.RIGHT, naEncode);
    
    return buildFormatResult(x, elements);
  }

  /**
   * 
   * @param x any R object (conceptually); typically numeric.

   * @param trim f ‘FALSE’, logical, numeric and complex values are
          right-justified to a common width: if ‘TRUE’ the leading
          blanks for justification are suppressed.
   * @param digits how many significant digits are to be used for numeric and
          complex ‘x’.  The default, ‘NULL’, uses ‘getOption(digits)’.
          This is a suggestion: enough decimal places will be used so
          that the smallest (in magnitude) number has this many
          significant digits, and also to satisfy ‘nsmall’.  (For the
          interpretation for complex numbers see ‘signif’.)

   * @param nsmall the minimum number of digits to the right of the decimal
          point in formatting real/complex numbers in non-scientific
          formats.  Allowed values are ‘0 <= nsmall <= 20’.

   * @param minWidth the _minimum_ field width or ‘NULL’ or ‘0’
          for no restriction.

   * @param zz
   * @param naEncode  should ‘NA’ strings be encoded?  Note this only
          applies to elements of character vectors, not to numerical or
          logical ‘NA’s, which are always encoded as ‘"NA"’.

   * @param scientific  Either a logical specifying whether elements of a real or
          complex vector should be encoded in scientific format, or an
          integer penalty (see ‘options("scipen")’).  Missing values
          correspond to the current default penalty.

   * @return
   */
  @Internal
  public static StringVector format(DoubleVector x, boolean trim, SEXP digits, int nsmall,
      SEXP minWidth, int zz, boolean naEncode, SEXP scientific ) {
       
    List<String> elements = formatNumericalElements(x);
    int width = calculateWidth(elements, minWidth);
    
    if(!trim) {
      elements = justify(elements, width, Justification.RIGHT, naEncode);
    }
    
    return buildFormatResult(x, elements);
  }

  @Internal
  public static StringVector format(IntVector x, boolean trim, SEXP digits, int nsmall,
      SEXP minWidth, int zz, boolean naEncode, SEXP scientific ) {
       
    List<String> elements = formatNumericalElements(x);
    int width = calculateWidth(elements, minWidth);
    
    if(!trim) {
      elements = justify(elements, width, Justification.RIGHT, naEncode);
    }
    
    return buildFormatResult(x, elements);
  }

  private static StringVector buildFormatResult(Vector x, List<String> elements) {
    StringVector.Builder result = new StringVector.Builder();
    result.addAll(elements);
    result.combineStructuralAttributesFrom(x);
    
    return result.build();
  }
  
  

  private static int calculateWidth(Iterable<String> elements, SEXP minWidth) {
    int width = 0;
    if(minWidth != Null.INSTANCE) {
      width = ((AtomicVector)minWidth).getElementAsInt(0);
    }

    for(String element : elements) {
      width = Math.max(width, stringWidth(element));
    }
    return width; 
  }
  
  private static int stringWidth(String element) {
    if(StringVector.isNA(element)) {
      return "NA".length();
    } else {
      return element.length();
    }
  }
  
  enum Justification {
    LEFT, RIGHT
  }
  
  private static List<String> justify(Iterable<String> elements, int width, Justification justification, boolean naEncode) {
    List<String> justified = Lists.newArrayList();
    for(String element : elements) {
      if(StringVector.isNA(element) && !naEncode) {
        justified.add(element);
      } else {
        String padding = padding(Math.max(0, width - stringWidth(element)));
        if(justification == Justification.LEFT) {
          justified.add(element + padding);
        } else {
          justified.add(padding + element);
        }
      }
    }
    return justified;
  }
  
  private static String padding(int count) {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i!=count;++i) {
      sb.append(' ');
    }
    return sb.toString();
  }

  private static List<String> formatNumericalElements(AtomicVector x) {
    DecimalFormat format = new DecimalFormat();
    format.setMinimumFractionDigits(0);
    format.setGroupingUsed(false);
    
    List<String> strings = Lists.newArrayList();
    for(int i=0;i!=x.length();++i) {
      if(x.isElementNA(i)) {
        strings.add("NA");
      } else {
        strings.add(format.format(x.getElementAsDouble(i)));
      }
    }
    return strings;
  }

  private static List<String> formatLogicalElements(AtomicVector x) {
    List<String> strings = Lists.newArrayList();     
    for(int i=0;i!=x.length();++i) {
      if(x.isElementNA(i)) {
        strings.add("NA");
      } else {
        strings.add(x.isElementTrue(i) ? "TRUE" : "FALSE");
      }
    }
    return strings;
  }

  private static List<String> formatCharacterElements(AtomicVector x, boolean naEncode) {
    
    List<String> strings = Lists.newArrayList();
    for(int i=0;i!=x.length();++i) {
      if(x.isElementNA(i) && naEncode) {
        strings.add("NA");
      } else {
        strings.add(x.getElementAsString(i));
      }
    }
    return strings;
  }
  
  /**
   * converts a length-one character string to an integer vector of (numeric) 
   * Unicode code points. The name (I think) is a misnomer because it has nothing to do with 
   * UTF-8 -- it seems to return code points. 
   *
   * @param x a single string
   * @return
   */
  @Internal
  public static IntVector utf8ToInt(String x) {
    if(StringVector.isNA(x)) {
      return new IntArrayVector(IntVector.NA);
    } else {
      IntArrayVector.Builder codePoints = new IntArrayVector.Builder(x.length());
      for(int i=0;i!=x.length();++i) {
        codePoints.set(i, x.codePointAt(i));
      }
      return codePoints.build();
    }
  }
  
  /**
   * 
   * @param x
   * @param multiple
   * @return
   */
  @Internal
  public static StringVector intToUtf8(AtomicVector x, boolean multiple) {
    if(multiple) {
      
      // return a vector of characters for each code point
      StringVector.Builder chars = new StringVector.Builder(x.length());
      for(int i=0;i!=x.length();++i) {
        if(x.isElementNA(i)) {
          chars.setNA(i);
        } else {
          int codePoint = x.getElementAsInt(i);
          if(codePoint == 0) {
            chars.set(i, "");
          } else {
            chars.set(i, new String(new int[] { codePoint }, 0, 1));
          }
        }
      }
      return chars.build();
      
    } else {
      
      // returns a single string built from all the codepoints
      if(x.containsNA()) {
        return StringVector.valueOf(StringVector.NA);
      } else {
        StringBuilder result = new StringBuilder();
        for(int i=0;i!=x.length();++i) {
          if(x.isElementNA(i)) {
          } 
          int codePoint = x.getElementAsInt(i);
          if(codePoint != 0) {
            result.appendCodePoint(codePoint);
          }
        }
        return StringVector.valueOf(result.toString());
      }
    }
    
  }
  
  @Internal("substr<-")
  public static StringVector setSubstring(String s, int start, int stop,String replace) {
    StringArrayVector.Builder result = new StringArrayVector.Builder();
    result.add(s.substring(0, start-1)+replace+s.substring(Math.min(stop,start-1+replace.length())));

    return result.build();
  }
  
  /**
   * Stub implementation of the iconv method. 
   * 
   * <p>
   * A fundamental difference between C-R and Renjin is that C-R stores strings as 
   * raw bytes and encodes to strings upon use. Therefore an R StringVector can have an
   * "encoding" attribute which dictates how the bytes should be interprted.
   * 
   * <p>
   * Renjin uses JVM Strings for StringVector storage, which are always stored in UTF-16
   * encoding. So transcoding a String from one encoding to another doesn't make much sense. 
   * 
   * <p>
   * There still may be some cases where this does make sense, but I think we'll need
   * concrete cases to properly wrap our heads around how it should work.
   * 
   * @param x the input StringVector
   * @param from the charset from which to convert
   * @param to the charset to which to convert
   * @param sub the string to use a substitute for unsupported characters
   * @param mark true if encoding should be marked (not implemented)
   * @param toRaw true if a ListVector of RawVectors should be returned instead of a StringVector
   * @return
   */
  @Internal
  public static Vector iconv(@InvokeAsCharacter Vector x, String from, String to, String sub,
                             boolean mark, boolean toRaw) {
    if(toRaw) {
      Charset destCharSet = RCharsets.getByName(to);
      ListVector.Builder result = new ListVector.Builder();
      for(int i=0;i!=x.length();++i) {
        result.add(new RawVector(
            x.getElementAsString(i)
              .getBytes(destCharSet)));
      }
      return result.build();
    } else {
      return x;
    }
  }

  @Internal
  @DataParallel(PreserveAttributeStyle.NONE)
  public static int strtoi(@Recycle String x, @Recycle(false) int base) {
    if(base == 0) {
      // For the default ‘base = 0L’, the base chosen from the string
      // representation of that element of ‘x’. The standard C
      //rules for choosing the base are that octal constants (prefix ‘0’
      // not followed by ‘x’ or ‘X’) and hexadecimal constants (prefix ‘0x’
      // or ‘0X’) are interpreted as base ‘8’ and ‘16’; all other strings
      // are interpreted as base ‘10’.
      if(x.startsWith("0x") || x.startsWith("0X")) {
        return Integer.parseInt(x.substring(2), 16);
      } else if(x.startsWith("0")) {
        return Integer.parseInt(x, 8);
      } else {
        return Integer.parseInt(x, 10);
      }
    } else if(base == 16 && (x.startsWith("0x") || x.startsWith("0X"))) {
      return Integer.parseInt(x.substring(2), 16);
    } else {
      return Integer.parseInt(x, base);
    }
  }
  
  
}
