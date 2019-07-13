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
package org.renjin.compiler.ir.tac.functions;

import org.apache.commons.compress.utils.Sets;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DynamicCallTranslator extends FunctionCallTranslator {

  public static final DynamicCallTranslator INSTANCE = new DynamicCallTranslator();

  /**
   * These operators may be generic, but they all evaluate all of their arguments
   * before dispatching or evaluating.
   */
  private static final Set<SEXP> STRICT_OPERATORS = Sets.newHashSet(
      Symbol.get("!"),
      Symbol.get(":"),
      Symbol.get("!="),
      Symbol.get("=="),
      Symbol.get(">"),
      Symbol.get("<"),
      Symbol.get(">"),
      Symbol.get(">="),
      Symbol.get("<="),
      Symbol.get("&"),
      Symbol.get("|"),
      Symbol.get("&&"),
      Symbol.get("||"),
      Symbol.get("+"),
      Symbol.get("-"),
      Symbol.get("*"),
      Symbol.get("/"),
      Symbol.get("^"),
      Symbol.get("%*%"),
      Symbol.get("%/%"),
      Symbol.get("%%"));

  private DynamicCallTranslator() {
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, FunctionCall call) {

    Expression functionExpr = functionExpression(builder, context, call.getFunction());

    // First check if any of the arguments include function calls that might be potentially
    // evaluated before calling the function

    if(requireStrictCheck(call)) {
      return translateWithStrictnessChecking(builder, context, call, functionExpr);

    } else {

      // If there are no function calls in arguments, or this is known
      // to be a function that is already strict, then we can evaluate the arguments directly.

      List<IRArgument> argumentList = new ArrayList<>();
      for (PairList.Node node : call.getArguments().nodes()) {

        argumentList.add(new IRArgument(node,
            builder.translateSimpleExpression(context, node.getValue())));
      }
      return new DynamicCall(builder.getRuntimeState(), call, functionExpr, argumentList);
    }
  }


  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall getterCall, Expression rhs) {
    if(getterCall.getFunction() instanceof Symbol) {
      Symbol getter = (Symbol) getterCall.getFunction();
      Symbol setter = Symbol.get(getter.getPrintName() + "<-");
      return new DynamicSetterCall(getterCall, new FunctionRef(setter), builder.simplify(rhs));

    } else {
      throw new NotCompilableException(getterCall, "Unsupported expression in complex assignment");
    }
  }

  private Expression translateWithStrictnessChecking(IRBodyBuilder builder, TranslationContext context, FunctionCall call, Expression functionExpr) {

    if(call.findEllipsisArgumentIndex() != -1) {
      throw new UnsupportedOperationException("TODO");
    }

    // A typical problem includes the following expression:
    //
    //   on.exit(close(con))
    //
    // Since we don't know the identity of the on.exit function at this point, we have to
    // guard against the possibility that the argument close(con) is not immediately evaluated (if at all)
    //
    // We will include this uncertainty in our code by using a fake 'strict' function that is only used
    // during compile time:

    //     arg0 ← promise(close(con))
    //     IF strict(on.exit, 0) GOTO L3
    //     arg0 ← close(con)
    // L3: on.exit( arg0 )

    // Here we skip the evaluation of the arguments, and on.exit passes a promise, rather than temp
    // as an argument.

    IRLabel callLabel = builder.newLabel();

    PairList.Node argument = (PairList.Node) call.getArguments();
    List<IRArgument> argumentList = new ArrayList<>();


    Temp[] temps = new Temp[call.getArguments().length()];
    int argumentIndex = 0;
    for (PairList.Node argumentNode : call.getArguments().nodes()) {
      if(argumentNode.getValue() instanceof FunctionCall) {
        Temp temp = builder.newTemp();
        temps[argumentIndex] = temp;
        builder.addStatement(new Assignment(temp, new PromiseExpr(argumentNode.getValue())));
      }
      argumentIndex++;
    }

    // Handle the first argument specially. Even generic builtins always evaluate their first argument.
    // Note that this may not be necessary if the first argument is not a function call
    if(argument.getValue() instanceof FunctionCall) {
      IRLabel strict0 = builder.newLabel();
      builder.addStatement(new IfStatement(new StrictFunction(functionExpr), strict0, callLabel));
      builder.addLabel(strict0);
      builder.addStatement(new Assignment(temps[0], builder.translateExpression(context, argument.getValue())));

      argumentList.add(new IRArgument(argument, temps[0]));

    } else {
      argumentList.add(new IRArgument(argument, builder.translateSimpleExpression(context, argument.getValue())));
    }

    // Second arguments are also specially handled as they are also unconditionally evaluated by
    // members of the Ops generic group
    if(argument.hasNextNode()) {
      argument = argument.getNextNode();
      if (argument.getValue() instanceof FunctionCall) {
        IRLabel strict1 = builder.newLabel();
        builder.addStatement(new IfStatement(new StrictFunction(functionExpr, argumentList), strict1, callLabel));
        builder.addLabel(strict1);
        builder.addStatement(new Assignment(temps[1], builder.translateExpression(context, argument.getValue())));
        argumentList.add(new IRArgument(argument, temps[1]));

      } else {
        argumentList.add(new IRArgument(argument,
            builder.translateSimpleExpression(context, argument.getValue())));
      }
    }

    // Remaining arguments, if any, are handled in a single block
    if(anyFunctionCallArgumentsRemain(argument)) {
      IRLabel strictn = builder.newLabel();
      builder.addStatement(new IfStatement(new StrictFunction(functionExpr, argumentList), strictn, callLabel));
      builder.addLabel(strictn);
    }

    argumentIndex = 2;
    while(argument.hasNextNode()) {
      argument = argument.getNextNode();

      if(argument.getValue() instanceof FunctionCall) {
        Temp temp = temps[argumentIndex];
        builder.addStatement(new Assignment(temp, builder.translateExpression(context, argument.getValue())));
        argumentList.add(new IRArgument(argument, temp));

      } else {
        argumentList.add(new IRArgument(argument,
            builder.translateSimpleExpression(context, argument.getValue())));
      }
    }

    // Now we're ready to make the call.
    builder.addLabel(callLabel);

    return new DynamicCall(builder.getRuntimeState(), call, functionExpr, argumentList);
  }

  private boolean anyFunctionCallArgumentsRemain(PairList.Node argument) {
    while(argument.hasNextNode()) {
      argument = argument.getNextNode();
      if(argument.getValue() instanceof FunctionCall) {
        return true;
      }
    }
    return false;
  }

  private boolean requireStrictCheck(FunctionCall call) {

    if (STRICT_OPERATORS.contains(call.getFunction())) {
      return false;
    }

    boolean requiresStrictCheck = false;
    for (SEXP value : call.getArguments().values()) {
      if(value instanceof FunctionCall || value instanceof ExpressionVector) {
        requiresStrictCheck = true;
      }
    }
    return requiresStrictCheck;
  }

  private Expression functionExpression(IRBodyBuilder builder, TranslationContext context, SEXP function) {
    if(function instanceof Symbol) {
      Symbol symbol = (Symbol) function;
      if(symbol.isReservedWord()) {
        return new BuiltinRef(symbol);
      } else {
        return new FunctionRef(symbol);
      }
    } else {
      return builder.translateSimpleExpression(context, function);
    }
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {

    builder.addStatement(new ExprStatement(translateToExpression(builder, context, call)));
  }


  private String functionName(FunctionCall call) {
    if(call.getFunction() instanceof Symbol) {
      return ((Symbol) call.getFunction()).getPrintName();
    } else {
      throw new IllegalStateException();
    }
  }

}
