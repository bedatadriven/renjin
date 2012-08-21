package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.PrimitiveCall;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;


public class DollarTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("$");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {
    Expression object = builder.translateExpression(context, call.getArgument(0));
    Symbol index = toSymbol(call.getArgument(1));
    
    return new PrimitiveCall(call, "$", builder.simplify(object), new Constant(index) ); 
  }

  private Symbol toSymbol(SEXP argument) {
    if(argument instanceof Symbol) {
      return (Symbol) argument;
    } else if(argument.length() == 1 && argument instanceof StringVector) {
      StringVector vector = (StringVector)argument;
      return Symbol.get(vector.getElementAsString(0));
    } else {
      throw new InvalidSyntaxException("Illegal index value: " + argument);
    }
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {
    
    // TODO: does x$a ever have any side effects? Maybe forces a promise?
    // if not, we can do a NO OP here.
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, call)));
    
  }
}
