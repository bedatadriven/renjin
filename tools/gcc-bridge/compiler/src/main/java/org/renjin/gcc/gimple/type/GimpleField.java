package org.renjin.gcc.gimple.type;

import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.repackaged.guava.base.Strings;

public class GimpleField {
  private int id;
  private int offset;
  private int size;
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
    return new GimpleFieldRef(id, name);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }


  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public boolean hasName() {
    return name != null;
  }

  @Override
  public String toString() {
    return "GimpleField[" + (addressed ? "&" : "") + name + ":" + type + "]";
  }
}
