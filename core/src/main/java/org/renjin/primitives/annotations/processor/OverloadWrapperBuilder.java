package org.renjin.primitives.annotations.processor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import org.renjin.eval.Context;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategies;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategy;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

import java.util.List;
import java.util.Map;

public class OverloadWrapperBuilder implements ApplyMethodContext {


  protected JCodeModel codeModel;
  protected JDefinedClass invoker;
  private PrimitiveModel primitive;
  private int arity;

  private List<JVar> arguments = Lists.newArrayList();
  private Map<JvmMethod.Argument, JVar> argumentMap = Maps.newHashMap();
  private JVar context;
  private JVar environment;

  public OverloadWrapperBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive, int arity) {
    this.codeModel = codeModel;
    this.invoker = invoker;
    this.primitive = primitive;
    this.arity = arity;
  }

  public void build() {
    JMethod method = invoker.method(JMod.STATIC | JMod.PUBLIC, codeModel.ref(SEXP.class), "doApply");
    context = method.param(Context.class, "context");
    environment = method.param(Environment.class, "environment");

    for(int i=0;i!=arity;++i) {
      JVar argument = method.param(SEXP.class, "arg" + i);
      arguments.add(argument);
    }

    IfElseBuilder matchSequence = new IfElseBuilder(method.body());
    for(JvmMethod overload : primitive.overloadsWithPosArgCountOf(arity)) {
      invokeOverload(overload, matchSequence._if(argumentsMatch(overload)));
    }
  }

  private void invokeOverload(JvmMethod overload, JBlock block) {
    JInvocation invocation = codeModel.ref(overload.getDeclaringClass())
            .staticInvoke(overload.getName());

    int argumentPos = 0;
    for(JvmMethod.Argument argument : overload.getAllArguments()) {
      if(argument.isContextual()) {
        if(argument.getClazz().equals(Context.class)) {
          invocation.arg(context);
        } else {
          throw new UnsupportedOperationException(argument.getClazz().getName());
        }
      } else {
        invocation.arg(convert(argument, arguments.get(argumentPos++)));
      }
    }

    block._return(invocation);
  }

  private JExpression convert(JvmMethod.Argument argument, JVar sexp) {
    return ArgConverterStrategies.findArgConverterStrategy(argument)
            .convertArgument(this, sexp);
  }

  /**
   * Compute the expression that will test whether the provided arguments
   * match the given overload.
   */
  private JExpression argumentsMatch(JvmMethod overload) {
    JExpression condition = null;
    List<JvmMethod.Argument> posFormals = overload.getPositionalFormals();
    for (int i = 0; i != posFormals.size(); ++i) {

      ArgConverterStrategy strategy = ArgConverterStrategies
              .findArgConverterStrategy(posFormals.get(i));

      JExpression argCondition = strategy.getTestExpr(codeModel, arguments.get(i));
      if(condition == null) {
        condition = argCondition;
      } else {
        condition = condition.cand(argCondition);
      }
    }
    return condition;
  }


  @Override
  public JExpression getContext() {
    return context;
  }

  @Override
  public JExpression getEnvironment() {
    return environment;
  }

  @Override
  public JClass classRef(Class<?> clazz) {
    return codeModel.ref(clazz);
  }
}
