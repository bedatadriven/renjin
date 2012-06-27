package org.renjin.primitives.annotations.processor;


import com.google.common.collect.Lists;
import com.sun.codemodel.*;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;

public abstract class ApplyMethodBuilder implements ApplyMethodContext {

  protected JCodeModel codeModel;
  protected JDefinedClass invoker;
  protected JVar context;
  protected JVar environment;
  protected JVar call;
  protected JVar args;
  protected PrimitiveModel primitive;
  protected JMethod method;

  protected JVar argumentIterator;

  private List<JVar> positionalArgumentSexps = Lists.newArrayList();
  private Map<JvmMethod.Argument,JVar> namedFlags;

  public ApplyMethodBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive) {
    this.codeModel = codeModel;
    this.invoker = invoker;
    this.primitive = primitive;
  }

  public void build() {
    method = invoker.method( JMod.PUBLIC, SEXP.class, "apply" );
    context = method.param(Context.class, "context");
    environment = method.param(Environment.class, "environment");
    call = method.param(FunctionCall.class, "call");
    args = method.param(PairList.class, "args");

    JTryBlock mainTryBlock = method.body()._try();
    catchArgumentExceptions(mainTryBlock);
    catchEvalExceptions(mainTryBlock);
    catchRuntimeExceptions(mainTryBlock);
    catchExceptions(mainTryBlock);

    argumentIterator = mainTryBlock.body().decl(classRef(ArgumentIterator.class), "argIt",
            _new(classRef(ArgumentIterator.class))
                    .arg(context)
                    .arg(environment)
                    .arg(args));


    apply(mainTryBlock.body());

  }

  protected abstract void apply(JBlock parent);

  private JInvocation invocation(JvmMethod overload, List<JExpression> argumentList) {
    JInvocation invocation = classRef(overload.getDeclaringClass()).staticInvoke(overload.getName());
    for(JExpression arg : argumentList) {
      invocation.arg(arg);
    }
    return invocation;
  }



  protected JExpression contextualExpression(JvmMethod.Argument formal) {
    if(formal.getClazz().equals(Context.class)) {
      return context;
    } else if(formal.getClazz().equals(Environment.class)) {
      return environment;
    } else if(formal.getClazz().equals(Context.Globals.class)) {
      return context.invoke("getGlobals");
    } else {
      throw new RuntimeException("Invalid contextual argument type: " + formal.getClazz());
    }
  }

  /**
   * Extracts the next argument at {@code positionalIndex} into a SEXP expression
   */
  protected JExpression nextArgAsSexp(boolean evaluated) {
    if(evaluated) {
      return JExpr.invoke(argumentIterator, "evalNext");
    } else {
      return JExpr.invoke(argumentIterator, "next");
    }
  }

  private void catchEvalExceptions(JTryBlock mainTryBlock) {
    JCatchBlock catchBlock = mainTryBlock._catch(classRef(EvalException.class));
    JVar e = catchBlock.param("e");
    catchBlock.body().invoke(e, "initContext").arg(context);
    catchBlock.body()._throw(e);
  }

  public JClass classRef(Class<?> clazz) {
    return (JClass) codeModel._ref(clazz);
  }

  private void catchArgumentExceptions(JTryBlock mainTryBlock) {
    mainTryBlock._catch((JClass) codeModel._ref(ArgumentException.class))
            .body().
            _throw(_new(codeModel._ref(EvalException.class))
                    .arg(context)
                    .arg(lit(primitive.argumentErrorMessage())));
  }


  private void catchRuntimeExceptions(JTryBlock mainTryBlock) {
    JCatchBlock catchBlock = mainTryBlock._catch(classRef(RuntimeException.class));
    JVar e = catchBlock.param("e");
    catchBlock.body()._throw(e);
  }

  private void catchExceptions(JTryBlock mainTryBlock) {
    JCatchBlock catchBlock = mainTryBlock._catch(classRef(Exception.class));
    JVar e = catchBlock.param("e");
    catchBlock.body()._throw(_new(classRef(EvalException.class)).arg(e));
  }

  @Override
  public JExpression getContext() {
    return context;
  }

  @Override
  public JExpression getEnvironment() {
    return environment;
  }
}