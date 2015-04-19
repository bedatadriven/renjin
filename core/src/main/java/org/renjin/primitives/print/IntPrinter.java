package org.renjin.primitives.print;

import com.google.common.base.Function;
import org.renjin.parser.NumericLiterals;


public class IntPrinter implements Function<Integer, String> {
  @Override
  public String apply(Integer integer) {
    return NumericLiterals.format(integer);
  }
}
