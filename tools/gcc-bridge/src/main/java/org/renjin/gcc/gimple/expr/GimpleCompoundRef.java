package org.renjin.gcc.gimple.expr;


public class GimpleCompoundRef extends GimpleLValue {

  private final GimpleVar var;
  private String member;

  public GimpleCompoundRef(GimpleVar var, String member) {
    this.var = var;
    this.member = member;
  }

  public GimpleVar getVar() {
    return var;
  }

  public String getMember() {
    return member;
  }

  @Override
  public String toString() {
    return var + "." + member;
  }
}
