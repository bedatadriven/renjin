package org.renjin.primitives.annotations.processor;


import com.sun.codemodel.*;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.processor.generic.GenericDispatchStrategy;
import org.renjin.primitives.annotations.processor.generic.OpsGroupGenericDispatchStrategy;
import org.renjin.primitives.annotations.processor.generic.SimpleDispatchStrategy;
import org.renjin.primitives.annotations.processor.generic.SummaryGroupGenericStrategy;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

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
  protected GenericDispatchStrategy genericDispatchStrategy;


  public ApplyMethodBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive) {
    this.codeModel = codeModel;
    this.invoker = invoker;
    this.primitive = primitive;
    this.genericDispatchStrategy = genericDispatchStrategy(primitive);
  }

  public void build() {
    declareMethod();

    ExceptionWrapper mainTryBlock = new ExceptionWrapper(codeModel, method.body(), context);
    catchArgumentExceptions(mainTryBlock);
    mainTryBlock.catchEvalExceptions();
    mainTryBlock.catchRuntimeExceptions();
    mainTryBlock.catchExceptions();

    argumentIterator = mainTryBlock.body().decl(classRef(ArgumentIterator.class), "argIt",
            _new(classRef(ArgumentIterator.class))
                    .arg(context)
                    .arg(environment)
                    .arg(args));


    apply(mainTryBlock.body());

  }

  @Override
  public JClass classRef(Class<?> clazz) {
    return codeModel.ref(clazz);
  }

  @Override
  public JCodeModel getCodeModel() {
    return codeModel;
  }

  protected void declareMethod() {
    method = invoker.method( JMod.PUBLIC, SEXP.class, "apply" );
    context = method.param(Context.class, "context");
    environment = method.param(Environment.class, "environment");
    call = method.param(FunctionCall.class, "call");
    args = method.param(PairList.class, "args");
  }

  protected void apply(JBlock parent) {

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

  public void catchArgumentExceptions(ExceptionWrapper mainTryBlock) {
    JCatchBlock catchBlock = mainTryBlock._catch((JClass) codeModel._ref(ArgumentException.class));
    JVar e = catchBlock.param("e");
    catchBlock.body().
            _throw(_new(codeModel._ref(EvalException.class))
                    .arg(context)
                    .arg(lit(primitive.argumentErrorMessage()))
                    .arg(e.invoke("getMessage")));
  }

  private GenericDispatchStrategy genericDispatchStrategy(PrimitiveModel primitive) {
    JvmMethod overload =  primitive.getOverloads().get(0);
    if (overload.isGroupGeneric()) {
      if (overload.getGenericGroup().equals("Ops")) {
        return new OpsGroupGenericDispatchStrategy(codeModel, primitive.getName());
      } else if (overload.getGenericGroup().equals("Summary")) {
        return new SummaryGroupGenericStrategy(codeModel, primitive.getName());
      } else {
        throw new GeneratorDefinitionException(
            "Group generic dispatch for group '" + overload.getGenericName()
                + "' is not implemented");
      }
    } else if (overload.isGeneric()) {
      return new SimpleDispatchStrategy(codeModel, primitive.getName());
    } else {
      return new GenericDispatchStrategy(codeModel);
    }
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
