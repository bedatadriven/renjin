package org.renjin.gcc.gimple.expr;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.statement.GimpleCall;

import java.util.List;

/**
 * Nested call expression introduced by the tree building phase
 */
public class GimpleCallExpr extends GimpleExpr {
  private GimpleExpr function;
  private List<GimpleExpr> arguments = Lists.newArrayList();

  public GimpleCallExpr(GimpleCall call) {
    this.function = call.getFunction();
    this.arguments = call.getOperands();
    this.setType(call.getLhs().getType());
  }

  public GimpleExpr getFunction() {
    return function;
  }

  public List<GimpleExpr> getArguments() {
    return arguments;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(function, predicate, results);
    findOrDescend(arguments, predicate, results);
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    if(predicate.apply(function)) {
      function = replacement;
      return true;
    }
    for (int i = 0; i < arguments.size(); i++) {
      if(predicate.apply(arguments.get(i))) {
        arguments.set(i, replacement);
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "gimple_call<" + function + ", " + Joiner.on(", ").join(arguments) + ">";
  }
}
