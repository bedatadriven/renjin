package org.renjin.primitives.annotations.processor;


import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JInvocation;

public class PassThroughApplyBuilder extends ApplyMethodBuilder {

  public PassThroughApplyBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive) {
    super(codeModel, invoker, primitive);
  }

  @Override
  public void build() {
    declareMethod();

    ExceptionWrapper mainTryBlock = new ExceptionWrapper(codeModel, method.body(), context);

    JInvocation invocation = codeModel.ref(primitive.getPassThrough().getDeclaringClass())
               .staticInvoke(primitive.getPassThrough().getName())
               .arg(context)
               .arg(environment)
               .arg(call);

    mainTryBlock.body()._return(invocation);
    mainTryBlock.catchEvalExceptions();
    mainTryBlock.catchRuntimeExceptions();
    mainTryBlock.catchExceptions();
  }
}
