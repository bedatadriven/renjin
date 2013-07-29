package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.JvmMethodCall;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.*;

import java.util.List;

public class BuiltinTranslator extends FunctionCallTranslator {

  public static final BuiltinTranslator INSTANCE = new BuiltinTranslator();

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    Primitives.Entry entry = Primitives.getBuiltinEntry(((PrimitiveFunction) resolvedFunction).getName());
    if(entry == null) {
      throw new NotCompilableException(call);
    }
    List<JvmMethod> methods = JvmMethod.findOverloads(entry.functionClass, entry.name, entry.methodName);
    return new JvmMethodCall(entry.name,
        methods, argumentNames(call.getArguments()),
        builder.translateArgumentList(context, call.getArguments()));
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

  private String[] argumentNames(PairList arguments) {
    String[] names = new String[arguments.length()];
    int i=0;
    for(PairList.Node node : arguments.nodes()) {
      if(node.hasTag()) {
        names[i] = node.getTag().getPrintName();
      }
    }
    return names;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
