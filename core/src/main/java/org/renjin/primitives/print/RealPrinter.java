package org.renjin.primitives.print;

import org.renjin.parser.NumericLiterals;
import org.renjin.repackaged.guava.base.Function;

public class RealPrinter implements Function<Double, String> {
  @Override
  public String apply(Double input) {
    return NumericLiterals.format(input, "NA");
  }
}
