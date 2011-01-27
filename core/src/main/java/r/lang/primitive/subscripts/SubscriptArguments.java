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

package r.lang.primitive.subscripts;

import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.List;

public class SubscriptArguments {

  private Vector source;
  private boolean drop = true;
  List<SEXP> subscripts;

  public SubscriptArguments(Context context, Environment rho, FunctionCall call) {
    source = EvalException.checkedCast(call.evalArgument(context, rho, 0));
    subscripts = Lists.newArrayList();

    for(PairList.Node node : ((PairList.Node)call.getArguments()).getNextNode().nodes()) {
      if(node.getName().equals("drop")) {
        drop = evaluateToBoolean(context, rho, node);

      } else {
        if(node.getValue() == Symbol.MISSING_ARG) {
          subscripts.add(node.getValue());
        } else {
          subscripts.add(node.getValue().evalToExp(context, rho));
        }
      }
    }
  }

  private boolean evaluateToBoolean(Context context, Environment rho, PairList.Node node) {
    return node.getValue().evalToExp(context, rho).asReal() != 0;
  }

  public Vector getSource() {
    return source;
  }

  public boolean isDrop() {
    return drop;
  }

  public int getSubscriptCount() {
    return subscripts.size();
  }

  public SEXP getSubscript(int index) {
    return subscripts.get(index);
  }

}
