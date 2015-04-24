package org.renjin.primitives.print;

import com.google.common.base.Function;
import org.renjin.sexp.RawVector;

public class RawPrinter implements Function<Byte, String> {
  @Override
  public String apply(Byte raw) {
    return RawVector.toString(raw);
  }
}
