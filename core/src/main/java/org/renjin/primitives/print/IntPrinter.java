package org.renjin.primitives.print;

import com.google.common.base.Function;
import org.renjin.parser.NumericLiterals;
import org.renjin.sexp.IntVector;


public class IntPrinter implements Function<Integer, String> {
  @Override
  public String apply(Integer integer) {
    if(IntVector.isNA(integer)) {
      return "NA"; 
    } else {
      return NumericLiterals.format(integer);
    }
  }
}
