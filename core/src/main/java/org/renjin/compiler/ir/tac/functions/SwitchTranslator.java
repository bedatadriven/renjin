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
package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.Symbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


public class SwitchTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, FunctionCall call) {
    // since "switch" is being used in the context of an expression, we need
    // to store its final value somewhere
    Temp result = builder.newTemp();

    build(builder, context, call, Optional.of(result));

    return result;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           FunctionCall call) {

    build(builder, context, call, Optional.empty());
  }


  private void build(IRBodyBuilder builder, TranslationContext context, FunctionCall call, Optional<Temp> result) {


    Iterator<PairList.Node> arguments = call.getArguments().nodes().iterator();

    // The first argument is used as the value on which to switch, regardless of its name
    if(!arguments.hasNext()) {
      throw new InvalidSyntaxException("argument \"EXPR\" is missing");
    }
    PairList.Node expr = arguments.next();
    SimpleExpression switchExpr = builder.translateSimpleExpression(context, expr.getValue());

    if(!arguments.hasNext()) {
      throw new InvalidSyntaxException("'switch' with no alternatives");
    }

    // The rest of the arguments are branches

    List<PairList.Node> branches = new ArrayList<>();
    while(arguments.hasNext()) {
      branches.add(arguments.next());
    }

    // Define our labels:
    //  At each "check" label, we test the switch for that branch
    //  At each "body" label, we evaluate the branch's body

    IRLabel[] checks = new IRLabel[branches.size() + 1];
    IRLabel[] body = new IRLabel[branches.size() + 1];

    for (int i = 0; i < branches.size(); i++) {
      checks[i] = builder.newLabel();
      body[i] = builder.newLabel();
    }

    // And finally the exit label, which marks that we are done with the
    // switch statement

    IRLabel exit = builder.newLabel();

    // We define a final "no match" branch, where the result value
    // is set to NULL if we need an expression

    IRLabel noMatch = builder.newLabel();
    if(result.isPresent()) {
      checks[branches.size()] = noMatch;
      body[branches.size()] = noMatch;

    } else {
      // Otherwise we just exit
      checks[branches.size()] = exit;
      body[branches.size()] = exit;
    }

    for (int i = 0; i < branches.size(); i++) {

      PairList.Node branch = branches.get(i);

      // First, define our check for this branch
      builder.addLabel(checks[i]);
      SwitchPredicate predicate;
      if(i + 1 == branches.size() && !branch.hasName()) {
        predicate = SwitchPredicate.finalUnnamedBranch(switchExpr, i + 1);
      } else {
        predicate = new SwitchPredicate(switchExpr, i + 1, branch.getRawTag());
      }

      builder.addStatement(new IfStatement(predicate, body[i], checks[i + 1]));

      // Now the body of this switch branch

      builder.addLabel(body[i]);

      if(branch.getValue() == Symbol.MISSING_ARG) {

        // Fall through to the next branch
        builder.addStatement(new GotoStatement(body[i + 1]));

      } else {

        // Evaluate this branch
        if(result.isPresent()) {
          SimpleExpression branchValue = builder.translateSimpleExpression(context, branch.getValue());
          builder.addStatement(new Assignment(result.get(), branchValue));
        } else {
          builder.translateStatements(context, branch.getValue());
        }
        builder.addStatement(new GotoStatement(exit));
      }
    }

    // Finally the "no match" branch
    if(result.isPresent()) {
      builder.addLabel(noMatch);
      builder.addStatement(new Assignment(result.get(), new Constant(Null.INSTANCE)));
    }

    // Fall through to exit...
    builder.addLabel(exit);
  }



}
