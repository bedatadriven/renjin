package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public class GimpleVarDecl {
  private int id;
  private GimpleType type;
  private String name;

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


  @Override
  public String toString() {
    return type + " " + (name == null ? "T" + Math.abs(id) : name);
  }

}
