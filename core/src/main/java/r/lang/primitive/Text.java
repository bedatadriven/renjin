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
import r.lang.AtomicVector;
import r.lang.ListVector;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.exception.EvalException;

import static com.google.common.collect.Iterables.transform;

public class Text {

  private Text() {}

  public static StringVector paste(ListVector arguments, String separator, String collapse) {

    int resultLength = arguments.longestElementLength();

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

  private static class StringElementAt implements Function<SEXP, String> {
    private int index;

    private StringElementAt(int index) {
      this.index = index;
    }

    @Override
    public String apply(SEXP input) {
      if(input instanceof AtomicVector) {
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

  public static StringVector gettext(String domain, StringVector messages) {
    return messages;
  }

  public static String ngettext(double n, String singularMessage, String pluralMessage,
                                String domain) {
    return n == 1 ? singularMessage : pluralMessage;
  }
}
