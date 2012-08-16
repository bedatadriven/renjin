package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;

import java.util.List;

public class GimpleCall extends GimpleIns {

	private GimpleExpr function;
	private List<GimpleExpr> operands;
  private GimpleLValue lhs;
	
	public GimpleCall(GimpleExpr function, GimpleLValue lhs, List<GimpleExpr> operands) {
		super();
    this.lhs = lhs;
		this.function = function;
		this.operands = operands;
	}

  public GimpleExpr getFunction() {
    return function;
  }

  public int getParamCount() {
    return operands.size();
  }

  public List<GimpleExpr> getParams() {
    return operands;
  }

  public GimpleLValue getLhs() {
    return lhs;
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
