package org.renjin.primitives.annotations.processor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.primitives.annotations.SessionScoped;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategies;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategy;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;

public class OverloadWrapperBuilder implements ApplyMethodContext {

  protected JCodeModel codeModel;
  protected JDefinedClass invoker;
  private PrimitiveModel primitive;
  private int arity;

  private List<JVar> arguments = Lists.newArrayList();
  private JVar context;
  private JVar environment;

  public OverloadWrapperBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive, int arity) {
    this.codeModel = codeModel;
    this.invoker = invoker;
    this.primitive = primitive;
    this.arity = arity;
  }

  public void build() {
    JMethod method = invoker.method(JMod.STATIC | JMod.PUBLIC, codeModel.ref(SEXP.class), "doApply")
        ._throws(Exception.class);

    context = method.param(Context.class, "context");
    environment = method.param(Environment.class, "environment");
    for(int i=0;i!=arity;++i) {
      JVar argument = method.param(SEXP.class, "arg" + i);
      arguments.add(argument);
    }

    /**
     * Tests the arguments given against those of each Java overload
     */
    IfElseBuilder matchSequence = new IfElseBuilder(method.body());
    List<JvmMethod> overloads = Lists.newArrayList( primitive.overloadsWithPosArgCountOf(arity) );

    /*
     * Sort the overloads so that we test more narrow types first, e.g.,
     * try "int" before falling back to "double".
     */
    Collections.sort( overloads, new OverloadComparator());
    for(JvmMethod overload : overloads) {
      /*
       * If the types match, invoke the Java method
       */
      invokeOverload(overload, matchSequence._if(argumentsMatch(overload)));
    }

    /**
     * No matching methods, throw an exception
     */
    matchSequence._else()._throw(_new(codeModel.ref(EvalException.class))
            .arg(typeMismatchErrorMessage(arguments)));
  }

  private JExpression typeMismatchErrorMessage(List<JVar> arguments) {
    JInvocation format = codeModel.ref(String.class).staticInvoke("format");
    format.arg(lit(typeMessageErrorFormat(arguments.size())));
    for(JVar arg : arguments) {
      format.arg(arg.invoke("getTypeName"));
    }
    return format;
  }

  private String typeMessageErrorFormat(int nargs) {
    StringBuilder message = new StringBuilder();
    message.append("Invalid argument:\n");
    message.append("\t").append(primitive.getName()).append("(");

    for(int i=0;i<nargs;++i) {
      if(i > 0) {
        message.append(", ");
      }
      message.append("%s");
    }
    message.append(")\n");
    message.append("\tExpected:");
    for(JvmMethod method : primitive.getOverloads()) {
      message.append("\n\t");
      method.appendFriendlySignatureTo(primitive.getName(), message);
    }
    return message.toString();
  }

  private Map<JvmMethod.Argument, JExpression> mapArguments(JvmMethod overload) {
    Map<JvmMethod.Argument, JExpression> argumentMap = Maps.newHashMap();

    int argumentPos = 0;
    for(JvmMethod.Argument argument : overload.getAllArguments()) {
      if(argument.isContextual()) {
        if(argument.getClazz().equals(Context.class)) {
          argumentMap.put(argument, context);
        } else if(argument.getClazz().equals(Environment.class)){
          argumentMap.put(argument, environment);
        } else if(argument.getClazz().equals(Session.class)) {
          argumentMap.put(argument, context.invoke("getSession"));
        } else if(argument.getClazz().getAnnotation(SessionScoped.class) != null) {
          argumentMap.put(argument, context.invoke("getSingleton").arg(JExpr.dotclass(codeModel.ref(argument.getClazz()))));
        } else {
          throw new UnsupportedOperationException(argument.getClazz().getName());
        }
      } else {
        argumentMap.put(argument, convert(argument, arguments.get(argumentPos++)));
      }
    }
    return argumentMap;
  }

  private void invokeOverload(JvmMethod overload, JBlock block) {

    if(overload.isRecycle()) {
      new RecycleLoopBuilder(codeModel, block, primitive, overload, mapArguments(overload))
            .build();
    } else {
      invokeSimpleMethod(overload, block);
    }
  }

  /**
   * Invokes with the JVM method simply (without recycling) using the
   * provided arguments.
   */
  private void invokeSimpleMethod(JvmMethod overload, JBlock block) {
    JInvocation invocation = codeModel.ref(overload.getDeclaringClass())
            .staticInvoke(overload.getName());

    Map<JvmMethod.Argument, JExpression> argumentMap = mapArguments(overload);

    for(JvmMethod.Argument argument : overload.getAllArguments()) {
      invocation.arg(argumentMap.get(argument));
    }
    CodeModelUtils.returnSexp(codeModel, block, overload, invocation);
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
    JExpression condition = JExpr.TRUE;
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

  @Override
  public JCodeModel getCodeModel() {
    return codeModel;
  }

}
