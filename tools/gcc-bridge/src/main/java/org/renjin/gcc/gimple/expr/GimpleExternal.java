package org.renjin.gcc.gimple.expr;

public class GimpleExternal extends GimpleExpr {

  private final String name;

  public GimpleExternal(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
