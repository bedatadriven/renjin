package org.renjin.primitives.print;

import com.google.common.base.Function;
import org.renjin.parser.StringLiterals;
import org.renjin.sexp.StringVector;

public class StringPrinter implements Function<String, String> {
  private boolean quote = true;
  
  /**
   * 
   * @param quote true if the strings should be double-quoted (")
   */
  public StringPrinter withQuotes(boolean quote) {
    this.quote = quote;
    return this;
  }
  
  @Override
  public String apply(String s) {
    if(quote || StringVector.isNA(s)) {
      return StringLiterals.format(s, "NA");        
    } else {
      return s;
    }
  }
}
