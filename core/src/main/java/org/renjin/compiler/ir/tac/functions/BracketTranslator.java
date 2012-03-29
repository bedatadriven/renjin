package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


public class BracketTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("{");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
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
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    if(call.getArguments() != Null.INSTANCE) {
      for(SEXP arg : call.getArguments().values()) {
        builder.translateStatements(context, arg);
      }
    }
  }
}
