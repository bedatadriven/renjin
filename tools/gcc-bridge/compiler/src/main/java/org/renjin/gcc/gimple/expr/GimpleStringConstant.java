package org.renjin.gcc.gimple.expr;

import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;

public class GimpleStringConstant extends GimpleConstant {

  private String value;
  
  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public void setType(GimpleType type) {
    if(!(type instanceof GimpleArrayType)) {
      throw new RuntimeException("Expected array type for StringConstant, got: " + type);
    }
    super.setType(type);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitStringConstant(this);
  }

  @Override
  public GimpleArrayType getType() {
    return (GimpleArrayType) super.getType();
  }

  @Override
  public String toString() {
    return "\"" + value.replace("\u0000", "<NULL>") + "\"";
  }
}
