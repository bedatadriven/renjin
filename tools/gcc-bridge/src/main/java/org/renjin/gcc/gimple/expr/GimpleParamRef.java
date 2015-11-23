package org.renjin.gcc.gimple.expr;

public class GimpleParamRef extends GimpleLValue implements GimpleSymbolRef {

  private int id;
  private String name;

  public GimpleParamRef() {
  }

  public GimpleParamRef(int id, String name) {
    this.id = id;
    this.name = name;
  }

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
