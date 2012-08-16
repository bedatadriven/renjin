package org.renjin.gcc.gimple.expr;

public class GimpleConstant extends GimpleExpr {
	private final Object value;
	
	public GimpleConstant(Object value) {
		this.value = value;
	}

  public Object getValue() {
    return value;
  }

  public Number getNumberValue() {
    if(value instanceof  Number) {
      return (Number) value;
    } else {
      throw new UnsupportedOperationException("Can't coerce constant to number: " + value);
    }
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
