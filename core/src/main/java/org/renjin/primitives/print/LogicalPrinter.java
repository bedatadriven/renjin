package org.renjin.primitives.print;

import com.google.common.base.Function;
import org.renjin.sexp.Logical;


public class LogicalPrinter implements Function<Logical, String> {
  @Override
  public String apply(Logical logical) {
    return logical.toString();
  }
}
