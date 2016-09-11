package org.renjin.primitives.print;

import org.renjin.repackaged.guava.base.Function;
import org.renjin.sexp.Logical;


public class LogicalPrinter implements Function<Logical, String> {
  @Override
  public String apply(Logical logical) {
    return logical.toString();
  }
}
