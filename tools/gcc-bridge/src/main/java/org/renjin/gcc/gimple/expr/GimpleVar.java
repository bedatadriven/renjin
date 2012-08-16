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
	public String toString() {
    if(version < 0) {
      return name;
    } else {
		  return name + "_" + version;
    }
	}
	
}
