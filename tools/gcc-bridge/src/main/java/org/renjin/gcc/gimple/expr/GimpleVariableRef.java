package org.renjin.gcc.gimple.expr;

public class GimpleVariableRef extends GimpleLValue implements SymbolRef {

  private String name;
  private int version;
  private int id;

  public GimpleVariableRef() {
  }

  public GimpleVariableRef(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + version;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GimpleVariableRef other = (GimpleVariableRef) obj;
    return name.equals(other.name) && version == other.version;
  }

  @Override
  public String toString() {
    if (name != null) {
      return name;
    } else {
      return "T" + Math.abs(id);
    }
  }

}
