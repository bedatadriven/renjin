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
package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.CompilerSpecialization;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.sexp.*;

public class SwitchFunction extends SpecialFunction {

  public SwitchFunction() {
    super("switch");
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    return doApply(context, rho, call, args);
  }

  private static SEXP doApply(Context context, Environment rho, FunctionCall call, PairList args) {

    ArgumentIterator argIt = new ArgumentIterator(context, rho, args);
    if(!argIt.hasNext()) {
      throw new EvalException("argument \"EXPR\" is missing");
    }

    PairList.Node exprNode = argIt.nextNode();
    if(exprNode.hasTag() && !"EXPR".startsWith(exprNode.getTag().getPrintName())) {
      throw new EvalException("supplied argument name '%s' does not match 'EXPR'", exprNode.getTag().getPrintName());
    }

    SEXP expr = context.evaluate(exprNode.getValue(), rho);

    checkExprArgument(expr);

    if (isBranchName(expr)) {
      return matchByName(context, rho, expr, argIt);
    } else {
      return matchByPosition(context, rho, expr, argIt);
    }
  }

  private static AtomicVector checkExprArgument(SEXP expr) {
    if(expr.length() != 1 || !(expr instanceof AtomicVector)) {
      throw new EvalException("EXPR must be a length 1 vector");
    }
    return (AtomicVector)expr;
  }

  private static boolean isBranchName(SEXP expr) {
    return expr instanceof StringVector;
  }

  private static SEXP matchByName(Context context, Environment rho, SEXP expr, ArgumentIterator argIt) {
    String name = branchName(expr);

    while(argIt.hasNext()) {
      PairList.Node argNode = argIt.nextNode();

      // Match by name
      if(argNode.hasTag() && argNode.getTag().getPrintName().equals(name)) {

        // Skip to the next non-missing argument, to support constructions like:
        // switch("a", a = , b = , c = 32)
        while(argNode.getValue() == Symbol.MISSING_ARG && argIt.hasNext()) {
          argNode = argIt.nextNode();
        }

        // Evaluate and return the matching argument
        return context.evaluate(argNode.getValue(), rho);
      }

      // If there are no matches, match the last unnamed argument, if one is present.
      if(!argNode.hasTag() && !argIt.hasNext()) {
        return context.evaluate(argNode.getValue(), rho);
      }
    }

    // If there are no matches, and no final unnamed argument, the result is NULL
    return Null.INSTANCE;
  }


  private static SEXP matchByPosition(Context context, Environment rho, SEXP expr, ArgumentIterator argIt) {

    int pos = branchNumber((AtomicVector) expr);

    if(!IntVector.isNA(pos) && pos > 0) {
      int argIndex = 1;
      while (argIt.hasNext()) {
        PairList.Node argNode = argIt.nextNode();
        if (argIndex == pos) {
          return context.evaluate(argNode.getValue(), rho);
        }
        argIndex++;
      }
    }

    return Null.INSTANCE;
  }

  public static String branchName(SEXP expr) {
    String name = expr.asString();
    if(StringVector.isNA(name)) {
      name = "NA";
    }
    return name;
  }

  public static int branchNumber(AtomicVector expr) {
    return expr.getElementAsInt(0);
  }

  @CompilerSpecialization
  public static boolean testFinal(SEXP expr, int branchNumber) {
    AtomicVector vector = checkExprArgument(expr);
    if(isBranchName(vector)) {
      // The last, unnamed argument in the switch() function matches
      // any character
      return true;
    }

    // If the EXPR argument is a branch number, then it still
    // needs to match the branch number
    return branchNumber(vector) == branchNumber;
  }

  @CompilerSpecialization
  public static boolean test(SEXP expr, int branchNumber) {
    AtomicVector vector = checkExprArgument(expr);
    if(isBranchName(expr)) {
      return false;
    }
    return branchNumber(vector) == branchNumber;
  }

  @CompilerSpecialization
  public static boolean test(SEXP expr, int branchNumber, String branchName) {
    AtomicVector vector = checkExprArgument(expr);
    if(isBranchName(expr)) {
      return branchName(expr).equals(branchName);
    } else {
      return branchNumber(vector) == branchNumber;
    }
  }
}
