package org.renjin.gcc.gimple.type;

import com.google.common.base.Strings;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;

public class GimpleField {
  private String name;
  private GimpleType type;
  private boolean addressed;

  public GimpleField() {
  }

  public GimpleField(String name, GimpleType type) {
    this.name = name;
    this.type = type;
  }

  public boolean isAddressed() {
    return addressed;
  }

  public void setAddressed(boolean addressed) {
    this.addressed = addressed;
  }

  
  public String getName() {
    return Strings.nullToEmpty(name);
  }
  public void setName(String name) {
    this.name = name;
  }
  public GimpleType getType() {
    return type;
  }
  public void setType(GimpleType type) {
    this.type = type;
  }
  
  
  public GimpleFieldRef refTo() {
    return new GimpleFieldRef(0, name);
  }  
}
