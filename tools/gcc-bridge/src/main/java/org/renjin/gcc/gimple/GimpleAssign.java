package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;

import java.util.List;

public class GimpleAssign extends GimpleIns {
	private GimpleOp op;
	private GimpleVar rhs;
	private List<GimpleExpr> arguments;
	
	GimpleAssign(GimpleOp op, GimpleVar rhs, List<GimpleExpr> arguments) {
		this.op = op;
		this.rhs = rhs;
		this.arguments = arguments;
	}
	
	public GimpleOp getOperator() {
		return op;
	}
	
	public GimpleVar getRHS() {
		return rhs;
	}
	
	public List<GimpleExpr> getOperands() {
		return arguments;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("gimple_assign<")
			.append(op)
			.append(", ")
			.append(rhs)
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
