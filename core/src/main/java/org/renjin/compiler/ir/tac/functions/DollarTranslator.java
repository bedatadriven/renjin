package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.NamedElementAccess;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.*;


public class DollarTranslator extends FunctionCallTranslator {

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           Function resolvedFunction, FunctionCall call) {

    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, Function resolvedFunction, FunctionCall call) {
    Expression object = builder.translateExpression(context, call.getArgument(0));

    SEXP nameArgument = call.getArgument(1);
    if(!(nameArgument instanceof Symbol)) {
      throw new NotCompilableException(call);
    }
    String name = ((Symbol) nameArgument).getPrintName();

    return new NamedElementAccess(object, name);
  }
}
