package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.type.GimpleType;

public class GimpleParameter {
	private final GimpleType type;
	private final String name;
	
	
	public GimpleParameter(GimpleType type, String name) {
		super();
		this.type = type;
		this.name = name;
	}

	public GimpleType getType() {
		return type;
	}



	public String getName() {
		return name;
	}



	@Override
	public String toString() {
		return type + " " + name;
	}
}
