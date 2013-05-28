package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public class GimpleStringConstant extends GimpleConstant {

  private String value;
  
  public void setValue(String value) {
    this.value = value;
  }

  @Override
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
  public GimpleArrayType getType() {
    return (GimpleArrayType) super.getType();
  }

  @Override
  public String toString() {
    return "\"" + value.replace("\u0000", "<NULL>") + "\"";
  }
}
