package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.MakeClosure;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

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
