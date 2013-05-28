package org.renjin.gcc.gimple.expr;

public class GimpleComponentRef extends GimpleLValue {

  private GimpleExpr value;
  private String member;

  public GimpleExpr getValue() {
    return value;
  }
  
  public void setMember(String member) {
    this.member = member;
  }

  public String getMember() {
    return member;
  }

  @Override
  public String toString() {
    return value + "." + member;
  }
}
