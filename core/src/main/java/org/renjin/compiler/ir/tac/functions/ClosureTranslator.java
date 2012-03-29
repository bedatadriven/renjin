package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.MakeClosure;
import org.renjin.eval.EvalException;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


public class ClosureTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("function");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {
   
    PairList formals = EvalException.checkedCast(call.getArgument(0));
    SEXP body = call.getArgument(1);
    SEXP source = call.getArgument(2);

    return new MakeClosure(builder.newFunction(formals, body));
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {

    // a closure whose value is not used has no side effects
    
  }

}
