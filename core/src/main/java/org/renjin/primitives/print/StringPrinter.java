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
package org.renjin.primitives.print;

import org.renjin.parser.StringLiterals;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.sexp.StringVector;

public class StringPrinter implements Function<String, String> {
  private char quote = 0;
  private String naString = "NA";
  private int width = -1;
  
  /**
   * 
   * @param quote true if the strings should be double-quoted (")
   */
  public StringPrinter setQuotes(boolean quote) {
    if(quote) {
      this.quote = '"';
    } else {
      this.quote = 0;
    }
    return this;
  }

  public StringPrinter setNaString(String naString) {
    this.naString = naString;
    return this;
  }

  @Override
  public String apply(String s) {
    StringBuilder sb = new StringBuilder();
    if(StringVector.isNA(s)) {
      return naString;
    }
    if(quote != 0) {
      sb.append(quote);
    }
    StringLiterals.appendEscaped(sb, s);
    if(quote != 0) {
      sb.append(quote);
    }
    return sb.toString();
  }

  public void setQuote(char quote) {
    this.quote = quote;
  }
}
