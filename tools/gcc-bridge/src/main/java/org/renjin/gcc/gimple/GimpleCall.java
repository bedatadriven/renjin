package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.util.List;

public class GimpleCall extends GimpleIns {

	private String function;
	private List<GimpleExpr> operands;
	
	public GimpleCall(String function, List<GimpleExpr> operands) {
		super();
		this.function = function;
		this.operands = operands;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("gimple_call <")
			.append(function)
			.append(", ");
		Joiner.on(", ").appendTo(sb, operands);
		sb.append(">");
		return sb.toString();
	}

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitCall(this);
  }
}
