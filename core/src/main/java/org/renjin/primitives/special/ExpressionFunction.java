package org.renjin.primitives.special;

import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

import java.util.List;

/**
 * Returns a vector of type "expression" containing its arguments
 * (unevaluated).
 */
public class ExpressionFunction extends SpecialFunction {

  public ExpressionFunction() {
    super("expression");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    NamesBuilder names = NamesBuilder.withInitialLength(0);
    List<SEXP> expressions = Lists.newArrayList();
    for(PairList.Node node : args.nodes()) {
      names.add(node.getName());
      expressions.add(node.getValue());
    }
    AttributeMap.Builder attributes = AttributeMap.builder();
    if(names.haveNames()) {
      attributes.setNames((StringVector)names.build());
    }
    return new ExpressionVector(expressions, attributes.build());
  }
}
