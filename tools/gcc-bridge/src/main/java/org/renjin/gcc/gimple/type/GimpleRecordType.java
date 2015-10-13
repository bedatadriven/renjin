package org.renjin.gcc.gimple.type;

public class GimpleRecordType extends AbstractGimpleType {
  private String name;
  private String id;
  

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    if(name == null) {
      return "anonymous_" + id;
    }
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "struct " + getName();
  }

  @Override
  public int sizeOf() {
    throw new UnsupportedOperationException();
  }
}
