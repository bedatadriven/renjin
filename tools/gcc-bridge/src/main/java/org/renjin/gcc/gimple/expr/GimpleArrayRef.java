package org.renjin.gcc.gimple.expr;

import com.google.common.collect.Iterables;

public class GimpleArrayRef extends GimpleLValue {
  private GimpleExpr array;
  private GimpleExpr index;

  public GimpleExpr getArray() {
    return array;
  }

  public void setValue(GimpleExpr value) {
    this.array = value;
  }

  public void setIndex(GimpleExpr index) {
    this.index = index;
  }

  public GimpleExpr getIndex() {
    return index;
  }

  @Override
  public Iterable<? extends SymbolRef> getSymbolRefs() {
    return Iterables.concat(array.getSymbolRefs(), index.getSymbolRefs());
  }

  @Override
  public String toString() {
    return array + "[" + index + "]";
  }
}
