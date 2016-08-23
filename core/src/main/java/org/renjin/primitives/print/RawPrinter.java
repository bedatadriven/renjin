package org.renjin.primitives.print;

import org.renjin.repackaged.guava.base.Function;
import org.renjin.sexp.RawVector;

public class RawPrinter implements Function<Byte, String> {
  @Override
  public String apply(Byte raw) {
    return RawVector.toString(raw);
  }
}
