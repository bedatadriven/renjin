package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.BuiltinCall;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PrimitiveFunction;

import java.util.List;

class BuiltinTranslator extends FunctionCallTranslator {

  public static final BuiltinTranslator INSTANCE = new BuiltinTranslator();

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    Primitives.Entry entry = Primitives.getBuiltinEntry(((PrimitiveFunction) resolvedFunction).getName());
    if(entry == null) {
      throw new NotCompilableException(call);
    }
    List<IRArgument> arguments = builder.translateArgumentList(context, call.getArguments());
    
    return new BuiltinCall(entry, arguments);
  }

  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder, TranslationContext context,
                                                Function resolvedFunction, FunctionCall getterCall,
                                                Expression rhs) {

    Primitives.Entry entry = Primitives.getBuiltinEntry(((PrimitiveFunction) resolvedFunction).getName());
    if(entry == null) {
      throw new NotCompilableException(getterCall);
    }
    
    List<IRArgument> arguments = builder.translateArgumentList(context, getterCall.getArguments());
    arguments.add(new IRArgument("value", rhs));

    return new BuiltinCall(entry, arguments);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
