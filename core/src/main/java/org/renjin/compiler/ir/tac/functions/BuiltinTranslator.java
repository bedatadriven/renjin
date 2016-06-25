package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.BuiltinCall;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.JvmMethodCall;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.PrimitiveFunction;

import java.util.List;

public class BuiltinTranslator extends FunctionCallTranslator {

  public static final BuiltinTranslator INSTANCE = new BuiltinTranslator();

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    Primitives.Entry entry = Primitives.getBuiltinEntry(((PrimitiveFunction) resolvedFunction).getName());
    if(entry == null) {
      throw new NotCompilableException(call);
    }
    String[] argumentNames = ArgumentNames.toArray(call.getArguments());
    List<Expression> arguments = builder.translateArgumentList(context, call.getArguments());
    
    return new BuiltinCall(entry, argumentNames, arguments);
  }

  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder, TranslationContext context,
                                                Function resolvedFunction, FunctionCall getterCall,
                                                Expression rhs) {

    Primitives.Entry entry = Primitives.getBuiltinEntry(((PrimitiveFunction) resolvedFunction).getName());
    if(entry == null) {
      throw new NotCompilableException(getterCall);
    }
    List<JvmMethod> methods = JvmMethod.findOverloads(entry.functionClass, entry.name, entry.methodName);

    int numGetterArgs = getterCall.getArguments().length();
    String[] argumentNames = new String[numGetterArgs+1];
    int argIndex = 0;
    for(PairList.Node argument : getterCall.getArguments().nodes()) {
      if(argument.hasTag()) {
        argumentNames[argIndex] = argument.getTag().getPrintName();
      }
      argIndex++;
    }
    // name of the RHS argument
    argumentNames[argIndex] = "value";

    List<Expression> arguments = builder.translateArgumentList(context, getterCall.getArguments());
    arguments.add(rhs);

    return new JvmMethodCall(entry.name,
        methods, argumentNames,
        builder.translateArgumentList(context, getterCall.getArguments()));
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
