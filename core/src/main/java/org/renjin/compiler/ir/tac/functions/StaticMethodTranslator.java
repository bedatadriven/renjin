package org.renjin.compiler.ir.tac.functions;

import com.google.common.collect.Lists;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.JvmMethodCall;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.reflection.FunctionBinding;
import org.renjin.invoke.reflection.MethodFunction;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;

import java.util.List;

public class StaticMethodTranslator extends FunctionCallTranslator {

  public static final StaticMethodTranslator INSTANCE = new StaticMethodTranslator();

  public StaticMethodTranslator() {
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context,
                                          Function resolvedFunction, FunctionCall call) {

    MethodFunction methodFunction = (MethodFunction)resolvedFunction;
    List<JvmMethod> overloads = Lists.newArrayList();
    for(FunctionBinding.Overload overload : methodFunction.getFunctionBinding().getOverloads()) {
      overloads.add(new JvmMethod(overload.getMethod()));
    }

    return new JvmMethodCall(methodFunction.getName(), overloads,
        ArgumentNames.toArray(call.getArguments()),
        builder.translateArgumentList(context, call.getArguments()));
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
