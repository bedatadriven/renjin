package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.expr.GimpleExpr;

public class GimpleReturn extends GimpleIns {
	private final GimpleExpr value;

	public GimpleReturn(GimpleExpr value) {
		super();
		this.value = value;
	}
	
	public GimpleExpr getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "gimple_return <" + value + ">";
	}

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitReturn(this);
  }
}
