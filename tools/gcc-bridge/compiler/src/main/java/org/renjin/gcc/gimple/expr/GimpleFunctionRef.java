package org.renjin.gcc.gimple.expr;

public class GimpleFunctionRef extends GimpleExpr {
  private int id;
  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    if (name != null) {
      return name;
    } else {
      return "T" + Math.abs(id);
    }
  }

}
