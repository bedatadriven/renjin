package org.renjin.primitives.annotations.processor;

import com.sun.codemodel.*;
import org.renjin.eval.EvalException;

import static com.sun.codemodel.JExpr._new;

public class ExceptionWrapper {

  private JTryBlock tryBlock;
  private JCodeModel codeModel;
  private JExpression context;

  public ExceptionWrapper(JCodeModel codeModel, JBlock parent, JExpression context) {
    this.codeModel = codeModel;
    this.context = context;
    tryBlock = parent._try();
  }

  public JBlock body() {
    return tryBlock.body();
  }

  public void catchEvalExceptions() {
    JCatchBlock catchBlock = tryBlock._catch(codeModel.ref(EvalException.class));
    JVar e = catchBlock.param("e");
    catchBlock.body().invoke(e, "initContext").arg(context);
    catchBlock.body()._throw(e);
  }

  public void catchRuntimeExceptions() {
    JCatchBlock catchBlock = tryBlock._catch(codeModel.ref(RuntimeException.class));
    JVar e = catchBlock.param("e");
    catchBlock.body()._throw(e);
  }

  public void catchExceptions() {
    JCatchBlock catchBlock = tryBlock._catch(codeModel.ref(Exception.class));
    JVar e = catchBlock.param("e");
    catchBlock.body()._throw(_new(codeModel.ref(EvalException.class)).arg(e));
  }

  public JCatchBlock _catch(JClass jClass) {
    return tryBlock._catch(jClass);
  }
}
