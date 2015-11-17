package org.renjin.gcc.gimple.expr;


public abstract class GimpleComplexPartExpr extends GimpleExpr {

  private GimpleExpr complexValue;

  public GimpleExpr getComplexValue() {
    return complexValue;
  }

  public void setComplexValue(GimpleExpr complexValue) {
    this.complexValue = complexValue;
  }

  @Override
  public Iterable<? extends SymbolRef> getSymbolRefs() {
    return complexValue.getSymbolRefs();
  }
}
