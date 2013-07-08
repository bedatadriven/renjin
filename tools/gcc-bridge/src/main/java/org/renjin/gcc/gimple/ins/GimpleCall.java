package org.renjin.gcc.gimple.ins;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;

import java.util.List;

public class GimpleCall extends GimpleIns {

  private GimpleExpr function;
  private List<GimpleExpr> arguments = Lists.newArrayList();
  private GimpleLValue lhs;

  public GimpleExpr getFunction() {
    return function;
  }

  public int getParamCount() {
    return arguments.size();
  }

  public List<GimpleExpr> getArguments() {
    return arguments;
  }

  public GimpleLValue getLhs() {
    return lhs;
  }

  public void setFunction(GimpleExpr function) {
    this.function = function;
  }

  public void setLhs(GimpleLValue lhs) {
    this.lhs = lhs;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(lhs);
    sb.append(" = ");
    sb.append("gimple_call <").append(function).append(", ");
    Joiner.on(", ").appendTo(sb, arguments);
    sb.append(">");
    return sb.toString();
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitCall(this);
  }
}
