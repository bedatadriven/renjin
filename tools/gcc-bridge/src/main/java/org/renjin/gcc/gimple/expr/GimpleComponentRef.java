package org.renjin.gcc.gimple.expr;

public class GimpleComponentRef extends GimpleLValue {

  private GimpleExpr value;
  private GimpleExpr member;

  public GimpleExpr getValue() {
    return value;
  }
  
  public void setMember(GimpleExpr member) {
    this.member = member;
  }

  public GimpleExpr getMember() {
    return member;
  }

  public String memberName() {
    if(member instanceof GimpleFieldRef) {
      return ((GimpleFieldRef) member).getName();
    }
    throw new UnsupportedOperationException(member.getClass().getSimpleName());
  }

  @Override
  public String toString() {
    return value + "." + member;
  }
}
