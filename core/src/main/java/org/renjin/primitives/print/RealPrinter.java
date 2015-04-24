package org.renjin.primitives.print;

import com.google.common.base.Function;
import org.renjin.parser.NumericLiterals;

public class RealPrinter implements Function<Double, String> {
  @Override
  public String apply(Double input) {
    return NumericLiterals.format(input, "NA");
  }
}
