package org.renjin.primitives.annotations.processor;

import com.sun.codemodel.*;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategies;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;

public class ApplyArrayArgsMethodBuilder implements ApplyMethodContext{

  private JCodeModel codeModel;
  private JDefinedClass invoker;
  private PrimitiveModel primitive;
  private JMethod method;
  private JVar context;
  private JVar environment;
  private JVar argNames;
  private JVar args;
  private JVar call;

  public ApplyArrayArgsMethodBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive) {
    this.codeModel = codeModel;
    this.invoker = invoker;
    this.primitive = primitive;
  }

  public void build() {
    declareMethod();

    ExceptionWrapper mainTryBlock = new ExceptionWrapper(codeModel, method.body(), context);
    for(Integer arity : primitive.getArity()) {
      JInvocation invocation = invoke("doApply")
              .arg(context)
              .arg(environment);

      for(int i=0;i<arity;++i) {
        invocation.arg(args.component(lit(i)));
      }
      mainTryBlock.body()._if(JExpr.direct("args.length").eq(JExpr.lit(arity)))
              ._then()._return(invocation);
    }

    mainTryBlock.catchEvalExceptions();
    mainTryBlock.catchRuntimeExceptions();
    mainTryBlock.catchExceptions();


    method.body()._throw(
            JExpr._new(codeModel.ref(EvalException.class))
                    .arg(lit(primitive.getName() + ": max arity is " + primitive.getMaxArity())));



  }


  public void buildVarArgs() {
    declareMethod();

    ExceptionWrapper mainTryBlock = new ExceptionWrapper(codeModel, method.body(), context);

    JvmMethod overload = primitive.getOverloads().get(0);
    VarArgParser parser = new VarArgParser(this, mainTryBlock.body(), overload);

    // convert the positional arguments
    convertArguments(parser.getArgumentProcessingBlock(), parser);

    // finally invoke the underlying function
    JInvocation invocation = classRef(overload.getDeclaringClass()).staticInvoke(overload.getName());
    for(JExpression argument : parser.getArguments()) {
      invocation.arg(argument);
    }

    CodeModelUtils.returnSexp(codeModel, mainTryBlock.body(), overload, invocation);


    mainTryBlock.catchEvalExceptions();
    mainTryBlock.catchRuntimeExceptions();
    mainTryBlock.catchExceptions();
  }

  private void declareMethod() {
    method = invoker.method(JMod.PUBLIC | JMod.STATIC, SEXP.class, "doApply");
    context = method.param(Context.class, "context");
    environment = method.param(Environment.class, "environment");
    call = method.param(FunctionCall.class, "call");
    argNames = method.param(String[].class, "argNames");
    args = method.param(SEXP[].class, "args");
  }

  private void convertArguments(JBlock parent, VarArgParser parser) {
    int index = 0;
    for(VarArgParser.PositionalArg posArg : parser.getPositionalArguments()) {
      parent.assign(posArg.getVariable(),
              convert(posArg.getFormal(), args.component(lit(index++))));
    }

    JForLoop forLoop = parent._for();
    JVar loopCounter = forLoop.init(codeModel._ref(int.class), "i", lit(parser.getPositionalArguments().size()));
    forLoop.test(loopCounter.lt(JExpr.direct("args.length")));
    forLoop.update(loopCounter.incr());

    forLoop.body().invoke(parser.getVarArgBuilder(), "add")
            .arg(argNames.component(loopCounter))
            .arg(args.component(loopCounter));
  }

  private JExpression convert(JvmMethod.Argument formal, JExpression sexp) {
    return ArgConverterStrategies.findArgConverterStrategy(formal).convertArgument(this, sexp);
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
