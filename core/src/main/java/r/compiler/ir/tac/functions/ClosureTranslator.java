package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRBlockBuilder;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.MakeClosure;
import r.compiler.ir.tac.operand.Operand;
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
  public Operand translateToExpression(IRBlockBuilder builder,
      TranslationContext context, FunctionCall call) {
   
    PairList formals = EvalException.checkedCast(call.getArgument(0));
    SEXP body = call.getArgument(1);
    SEXP source = call.getArgument(2);

    return new MakeClosure(builder.newFunction(formals, body));
  }

  @Override
  public void addStatement(IRBlockBuilder builder, TranslationContext context,
      FunctionCall call) {

    // a closure whose value is not used has no side effects
    
  }

}
