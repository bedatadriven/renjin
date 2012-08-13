package org.renjin.gcc.gimple.expr;

public class GimpleVar extends GimpleExpr {

	private final String name;
  private final int version;
	
	public GimpleVar(String name, int version) {
		this.name = name;
    this.version = version;
	}

	public String getName() {
		return name;
	}

  public int getVersion() {
    return version;
  }

  @Override
	public String toString() {
		return name + "_" + version;
	}
	
}
