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

import com.google.common.base.Joiner;
import r.lang.*;
import r.lang.primitive.binding.TypeConverter;

import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static java.util.Collections.max;

public class Text {

  private Text() {}

  public static SEXP paste(ListExp args, String seperator, NullExp collapse) {
    List<StringExp> strings = TypeConverter.convertElements(args, StringExp.class);

    int resultLength = max(transform(strings, Functions.length()));
    String results[] = new String[resultLength];
    
    for(int i=0;i!=resultLength;++i) {
      results[i] = Joiner.on(seperator).join(transform(strings, Functions.elementAt(i)));
    }
    
    return new StringExp( results );
  }

  public static String paste(ListExp args, String seperator, String collapse) {
    List<StringExp> strings = TypeConverter.convertElements(args, StringExp.class);

    int resultLength = max(transform(strings, Functions.length()));
    StringBuilder result = new StringBuilder();

    for(int i=0;i!=resultLength;++i) {
      if(i != 0) {
        result.append(collapse);
      }
      Joiner.on(seperator).appendTo(result, transform(strings, Functions.elementAt(i)));
    }

    return result.toString() ;
  }


  public static SEXP substitute(SEXP... values) {
    // TODO!
    return NullExp.INSTANCE;
  }

}
