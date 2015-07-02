package org.renjin.gcc.gimple.expr;

/**
 * Gimple expression which evaluates to the name of a field
 */
public class GimpleFieldRef extends GimpleExpr {


  private String name;
  private int id;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return name;
  }
}
