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

package r.lang.primitive.special;

import com.google.common.collect.Iterables;
import r.lang.*;
import r.lang.exception.EvalException;

public class SwitchFunction extends SpecialFunction {

  @Override
  public String getName() {
    return "switch";
  }

  @Override
  public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList args) {
    EvalException.check(call.length() > 1, "argument \"EXPR\" is missing");

      SEXP expr = call.evalArgument(context, rho, 0);
      EvalException.check(expr.length() == 1, "EXPR must return a length 1 vector");

      DotExp branchPromises  = (DotExp) call.getArgument(1).evalToExp(context, rho);
      Iterable<PairList.Node> branches = branchPromises.getPromises().nodes();

      if(expr instanceof StringVector) {
        String name = ((StringVector) expr).getElementAsString(0);
        SEXP partialMatch = null;
        int partialMatchCount = 0;
        for(PairList.Node node : branches) {
          if(node.hasTag()) {
            String branchName = node.getTag().getPrintName();
            if(branchName.equals(name)) {
              return node.getValue().evaluate(context, rho);
            } else if(branchName.startsWith(name)) {
              partialMatch = node.getValue();
              partialMatchCount ++;
            }
          }
        }
        if(partialMatchCount == 1) {
          return partialMatch.evaluate(context, rho);
        } else if(Iterables.size(branches) > 0) {
          PairList.Node last = Iterables.getLast(branches);
          if(!last.hasTag()) {
            return last.getValue().evaluate(context, rho);
          }
        }

      } else if(expr instanceof AtomicVector) {
        int branchIndex = ((AtomicVector) expr).getElementAsInt(0);
        if(branchIndex >= 1 && branchIndex <= Iterables.size(branches)) {
          return Iterables.get(branches, branchIndex-1).getValue().evaluate(context, rho);
        }
      }

      return EvalResult.visible( Null.INSTANCE );
    }
}
