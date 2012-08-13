package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.util.List;

public class GimpleConditional extends GimpleIns {
//  gimple_cond <ne_expr, i_4, j_6, NULL, NULL>
//    goto <bb 46>;
//  else
//    goto <bb 47>;
	
	private GimpleOp operator;
	private List<GimpleExpr> operands;
	private GimpleLabel trueTarget;
	private GimpleLabel falseTarget;
	
	GimpleConditional() {
		
	}
	
	void setOperator(GimpleOp op) {
		this.operator = op;
	}
	
	void setOperands(List<GimpleExpr> operands) {
		this.operands = operands;
	}

	public GimpleLabel getTrueTarget() {
		return trueTarget;
	}

	public void setTrueTarget(GimpleLabel trueTarget) {
		this.trueTarget = trueTarget;
	}

	public GimpleLabel getFalseTarget() {
		return falseTarget;
	}

	public void setFalseTarget(GimpleLabel falseTarget) {
		this.falseTarget = falseTarget;
	}

	public GimpleOp getOperator() {
		return operator;
	}

	public List<GimpleExpr> getOperands() {
		return operands;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("gimple_cond<")
		  .append(operator)
		  .append(",");
		
		Joiner.on(", ").appendTo(sb, operands);
		sb.append("> goto <")
		  .append(trueTarget)
		  .append("> else goto <")
		  .append(falseTarget)
		  .append(">");
		return sb.toString();
	}


  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitConditional(this);
  }
}
