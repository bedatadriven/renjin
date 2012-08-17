package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.type.GimpleType;

public class GimpleVarDecl {
	private final GimpleType type;
	private final String name;
  private final Object constantValue;
	
	public GimpleVarDecl(GimpleType type, String name) {
		super();
		this.type = type;
		this.name = name;
    constantValue = null;
	}

  public GimpleVarDecl(GimpleType type, String name, Object constantValue) {
		this.type = type;
		this.name = name;
    this.constantValue = constantValue;
  }

  public GimpleType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

  public Object getConstantValue() {
    return constantValue;
  }

  @Override
	public String toString() {
		return type + " " + name;
	}
	
}
