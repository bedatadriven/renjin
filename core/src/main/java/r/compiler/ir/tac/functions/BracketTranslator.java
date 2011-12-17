package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.IRScopeBuilder;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.Expression;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;

public class BracketTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("{");
  }

  @Override
  public Expression translateToExpression(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    if(call.getArguments().length() == 0) {
      return new Constant(Null.INSTANCE);
    } else {
      for(PairList.Node arg : call.getArguments().nodes()) {
        if(arg.hasNextNode()) {
          builder.translateStatements(context, arg.getValue()); 
        } else {
          return builder.translateExpression(context, arg.getValue());
        }
      }
      throw new Error("unreachable");
    }
  }

  @Override
  public void addStatement(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    if(call.getArguments() != Null.INSTANCE) {
      for(SEXP arg : call.getArguments().values()) {
        builder.translateStatements(context, arg);
      }
    }
  }
}
