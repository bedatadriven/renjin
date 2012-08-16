package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;

import java.util.List;

public class GimpleAssign extends GimpleIns {
	private GimpleOp op;
	private GimpleLValue lhs;
	private List<GimpleExpr> arguments;
	
	GimpleAssign(GimpleOp op, GimpleLValue lhs, List<GimpleExpr> arguments) {
		this.op = op;
		this.lhs = lhs;
		this.arguments = arguments;
	}
	
	public GimpleOp getOperator() {
		return op;
	}
	
	public GimpleLValue getLHS() {
		return lhs;
	}
	
	public List<GimpleExpr> getOperands() {
		return arguments;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("gimple_assign<")
			.append(op)
			.append(", ")
			.append(lhs)
			.append(", ");
		Joiner.on(", ").appendTo(sb, arguments);
		sb.append(">");
		return sb.toString();
	}

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitAssignment(this);
  }
}
