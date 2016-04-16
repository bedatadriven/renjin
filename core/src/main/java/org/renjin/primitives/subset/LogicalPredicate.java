package org.renjin.primitives.subset;

import org.renjin.sexp.LogicalVector;

/**
 * Selects elements using a logical vector, recycling elements as necessary 
 */
class LogicalPredicate implements IndexPredicate {
  private LogicalVector subscript;

  public LogicalPredicate(LogicalVector subscript) {
    assert subscript.length() != 0;
    this.subscript = subscript;
  }

  @Override
  public boolean apply(int index) {
    int mask = subscript.getElementAsRawLogical(index % subscript.length());
    return mask == 1;
  }
}
