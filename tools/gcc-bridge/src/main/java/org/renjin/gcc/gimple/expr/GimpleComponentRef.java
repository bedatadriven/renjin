package org.renjin.gcc.gimple.expr;

import com.google.common.base.Predicate;

import java.util.List;

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
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(value, predicate, results);
    findOrDescend(member, predicate, results);
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    if(predicate.apply(value)) {
      value = replacement;
      return true;
    }
    if(value.replace(predicate, replacement)) {
      return true;
    }

    if(predicate.apply(member)) {
      member = replacement;
      return true;
    }
    if(member.replace(predicate, replacement)) {
      return true;
    }
    
    return false;
  }

  @Override
  public String toString() {
    return value + "." + member;
  }
}
