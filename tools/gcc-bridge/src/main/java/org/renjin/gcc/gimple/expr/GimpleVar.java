package org.renjin.gcc.gimple.expr;

public class GimpleVar extends GimpleLValue {

	private final String name;
  private final int version;
	
	public GimpleVar(String name, int version) {
		this.name = name;
    this.version = version;
	}

  public GimpleVar(String varName) {
    this(varName, -1);
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
    GimpleVar other = (GimpleVar) obj;
    return name.equals(other.name) && version == other.version;
  }

  @Override
	public String toString() {
    if(version < 0) {
      return name;
    } else {
		  return name + "_" + version;
    }
	}
	
}
