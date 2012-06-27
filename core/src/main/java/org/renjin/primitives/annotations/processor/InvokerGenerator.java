package org.renjin.primitives.annotations.processor;


import com.sun.codemodel.*;
import org.renjin.sexp.BuiltinFunction;
import org.renjin.sexp.SpecialFunction;

import java.io.IOException;

public class InvokerGenerator {

  private final JCodeModel codeModel;

  public InvokerGenerator(JCodeModel codeModel) {
    this.codeModel = codeModel;
  }

  public final void generate(PrimitiveModel model) throws JClassAlreadyExistsException, IOException {
    JDefinedClass invoker = codeModel._class(  WrapperGenerator.toFullJavaName(model.getName()) ); //Creates a new class
    if(model.isSpecial()) {
      invoker._extends(SpecialFunction.class);
    } else {
      invoker._extends(BuiltinFunction.class);
    }

    JMethod defaultConstructor = invoker.constructor(JMod.PUBLIC);
    defaultConstructor.body().invoke("super").arg(JExpr.lit(model.getName()));

    if(model.hasVargs() && model.getOverloads().size() > 1) {
      throw new GeneratorDefinitionException(model.getName() + ": If var args are used, multiple overloads cannot be used");
    }

    if(model.hasVargs()) {
      VarArgApplyBuilder apply = new VarArgApplyBuilder(codeModel, invoker, model);
      apply.build();
    } else {
      FixedArityApplyBuilder apply = new FixedArityApplyBuilder(codeModel, invoker, model);
      apply.build();

      for(Integer arity : model.getArity()) {
        OverloadWrapperBuilder doApply = new OverloadWrapperBuilder(codeModel, invoker, model, arity);
        doApply.build();
      }
    }
  }
}
