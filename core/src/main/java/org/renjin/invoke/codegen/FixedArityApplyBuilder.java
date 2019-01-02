/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.eval.EvalException;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.model.PrimitiveModel;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.SEXP;

import java.util.List;

import static com.sun.codemodel.JExpr.*;

/**
 * Implements the apply() method of {@code PrimitiveFunction} for methods
 * that do not have var arguments, but have one or more overloads of fixed arity.
 */
public class FixedArityApplyBuilder extends ApplyMethodBuilder {

  public FixedArityApplyBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive) {
    super(codeModel, invoker, primitive);
  }

  @Override
  protected void apply(JBlock parent) {

    List<JExpression> arguments = Lists.newArrayList();

    for(int i=0; i<primitive.maxPositionalArgs();++i) {

      List<JvmMethod> overloads = primitive.overloadsWithPosArgCountOf(i);
      if(!overloads.isEmpty()) {

        // if this is the last argument, then we call the wrapper
        // for this overload
        arityMatches(parent._if(lastArgument())._then(), arguments);
      }

      JVar argument = parent.decl(classRef(SEXP.class), "s" + i, nextArgAsSexp(primitive.isEvaluated(i)));
      arguments.add(argument);

      if(i == 0) {
        genericDispatchStrategy.afterFirstArgIsEvaluated(this, call, args, parent, argument);
      }
    }

    arityMatches(parent._if(lastArgument())._then(), arguments);

    // if we still have arguments left, there are too many
    parent._throw(_new(classRef(EvalException.class)).arg(invalidArityMessage()));
  }

  private JExpression invalidArityMessage() {
    return lit(primitive.getName() + ": too many arguments, expected at most " + primitive.getMaxArity() + ".");
  }

  private void arityMatches(JBlock parent, List<JExpression> arguments) {
    genericDispatchStrategy.beforeTypeMatching(this, call, arguments, parent);
    parent._return(invokeWrapper(arguments));
  }

  private JExpression invokeWrapper(List<JExpression> arguments) {
    JInvocation invocation = JExpr.invoke(JExpr._this(), "doApply");
    invocation.arg(context);
    invocation.arg(environment);
    for(JExpression argument : arguments) {
      invocation.arg(argument);
    }
    return invocation;
  }

  private JExpression lastArgument() {
    return invoke(argumentIterator, "hasNext").not();
  }
}
