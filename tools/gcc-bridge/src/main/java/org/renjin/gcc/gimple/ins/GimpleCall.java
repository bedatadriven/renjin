package org.renjin.gcc.gimple.ins;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.SymbolRef;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  public Integer getLineNumber() {
    if(lhs == null) {
      return null;
    } else {
      return lhs.getLine();
    }
  }


  @Override
  public Set<SymbolRef> getUsedExpressions() {
    Set<SymbolRef> used = new HashSet<>();
    for (GimpleExpr operand : arguments) {
      Iterables.addAll(used, operand.getSymbolRefs());
    }
    return used;
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

  @Override
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    if(lhs != null) {
      return predicate.apply(lhs);
    } else {
      return false;
    }
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(predicate.apply(lhs)) {
      lhs = (GimpleLValue) newExpr;
    }
    replaceAll(predicate, arguments, newExpr);
  }
}
