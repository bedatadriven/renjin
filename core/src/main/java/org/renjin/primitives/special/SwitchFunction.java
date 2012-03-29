/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SpecialFunction;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

import com.google.common.collect.Iterables;

public class SwitchFunction extends SpecialFunction {

  public SwitchFunction() {
    super("switch");
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    return doApply(context, rho, call, args);
  }

  private static SEXP doApply(Context context, Environment rho, FunctionCall call, PairList args) {
    EvalException.check(call.length() > 1, "argument \"EXPR\" is missing");

    SEXP expr = context.evaluate(args.getElementAsSEXP(0),rho);
    EvalException.check(expr.length() == 1, "EXPR must return a length 1 vector");

    Iterable<PairList.Node> branches = Iterables.skip(args.nodes(), 1);

    if(expr instanceof StringVector) {
      String name = ((StringVector) expr).getElementAsString(0);
      if(StringVector.isNA(name)) {
        context.setInvisibleFlag();
        return Null.INSTANCE;
      }
      SEXP partialMatch = null;
      int partialMatchCount = 0;
      for(PairList.Node node : branches) {
        if(node.hasTag()) {
          String branchName = node.getTag().getPrintName();
          if(branchName.equals(name)) {
            return context.evaluate( nextNonMissing(node), rho);
          } else if(branchName.startsWith(name)) {
            partialMatch = nextNonMissing(node);
            partialMatchCount ++;
          }
        }
      }
      if(partialMatchCount == 1) {
        return context.evaluate( partialMatch, rho);
      } else if(Iterables.size(branches) > 0) {
        PairList.Node last = Iterables.getLast(branches);
        if(!last.hasTag()) {
          return context.evaluate( last.getValue(), rho);
        }
      }

    } else if(expr instanceof AtomicVector) {
      int branchIndex = ((AtomicVector) expr).getElementAsInt(0);
      if(branchIndex >= 1 && branchIndex <= Iterables.size(branches)) {
        return context.evaluate( Iterables.get(branches, branchIndex-1).getValue(), rho);
      }
    }
    // no match
    return Null.INSTANCE;
  }

  private static SEXP nextNonMissing(PairList.Node node) {
    do {
      if(node.getValue() != Symbol.MISSING_ARG) {
        return node.getValue();
      }
      if(!node.hasNextNode()) {
        return Null.INSTANCE;
      }
      node = node.getNextNode();
    } while(true);
  }
  
  public static SEXP matchAndApply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] arguments) {
    PairList.Builder args = new PairList.Builder();
    for(int i =0;i!=arguments.length;++i) {
      args.add(argumentNames[i], arguments[i]);
    }
    return doApply(context, rho, call, args.build());
  }
}
