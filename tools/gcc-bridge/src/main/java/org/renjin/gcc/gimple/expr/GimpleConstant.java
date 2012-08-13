package org.renjin.gcc.gimple.expr;

public class GimpleConstant extends GimpleExpr {
	private final Object value;
	
	public GimpleConstant(Object value) {
		this.value = value;
	}

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
