/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.invoke.codegen;


import com.sun.codemodel.*;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.codegen.generic.*;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.model.PrimitiveModel;
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
    if (primitive.getName().equals("%*%")) {
      return new MatrixMultDispatchStrategy(codeModel);
    } else if (overload.isGroupGeneric()) {
      if (overload.getGenericGroup().equals("Ops")) {
        return new OpsGroupGenericDispatchStrategy(codeModel, primitive.getName());
      } else if (overload.getGenericGroup().equals("Summary")) {
        return new SummaryGroupGenericStrategy(codeModel, primitive.getName());
      } else {
        return new GroupDispatchStrategy(codeModel, overload.getGenericGroup(), primitive.getName());
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

  @Override
  public JExpression getCall() {
    return call;
  }

}
