package org.renjin.primitives.print;

import org.renjin.parser.NumericLiterals;
import org.renjin.repackaged.guava.base.Function;
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
