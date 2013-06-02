package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public class GimpleVarDecl {
  private int id;
  private GimpleType type;
  private String name;
  private GimpleExpr value;

  public GimpleVarDecl() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public GimpleType getType() {
    return type;
  }

  public void setType(GimpleType type) {
    this.type = type;
  }

  public String getName() {
    if (name != null) {
      return name;
    } else {
      return "T" + Math.abs(id);
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  public GimpleExpr getValue() {
    return value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return type + " " + (name == null ? "T" + Math.abs(id) : name);
  }

}
