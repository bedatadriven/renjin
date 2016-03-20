package org.renjin.gcc.gimple.type;

public class GimpleRecordType extends AbstractGimpleType {
  private String name;
  private String id;

  public GimpleRecordType() {
  }
  
  public GimpleRecordType(GimpleRecordTypeDef typeDef) {
    this.id = typeDef.getId();
    this.setSize(typeDef.getSize());
    this.setName(typeDef.getName());
  }

  public GimpleRecordType(String id) {
    this.id = id;
  }

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
    return getSize() / 8;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GimpleRecordType that = (GimpleRecordType) o;

    return id.equals(that.id);

  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
