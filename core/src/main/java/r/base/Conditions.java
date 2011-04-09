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

package r.base;

import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.lang.exception.EvalException;

public class Conditions {

  private Conditions() {}


  @Primitive(".addCondHands")
  public static SEXP addConditionHandlers(@Current Context context,
                                          SEXP classesExp,
                                          SEXP handlersExp,
                                          Environment parentEnv,
                                          Environment target,
                                          LogicalVector calling) {

    // this is quite literally translated from errors.c
    // I don't quite know what it does :-)

    if(classesExp == Null.INSTANCE || handlersExp == Null.INSTANCE) {
      return context.getGlobals().conditionHandlerStack;
    }

    if( !(classesExp instanceof StringVector) ||
        !(handlersExp instanceof ListVector)  ||
        classesExp.length() != handlersExp.length() ) {
      throw new EvalException("bad handler data");
    }

    StringVector classes = (StringVector) classesExp;
    ListVector handlers = (ListVector) handlersExp;

    int n = handlersExp.length();
    PairList oldStack = context.getGlobals().conditionHandlerStack;

    ListVector result = ListVector.newBuilder()
        .add(Null.INSTANCE)
        .add(Null.INSTANCE)
        .add(Null.INSTANCE)
        .build();

    PairList newStack = oldStack;

    for (int i = n - 1; i >= 0; i--) {
      ListVector entry = ListVector.newBuilder()
          .add(classes.getElementAsSEXP(i))
          .add(parentEnv)
          .add(handlers.getElementAsSEXP(i))
          .add(target)
          .add(result)
          .build();
      // SETLEVELS(entry, calling);

      newStack = new PairList.Node(entry, newStack);
    }

    context.getGlobals().conditionHandlerStack = newStack;

    return oldStack;
  }

  @Primitive(".signalCondition")
  public static void signalCondition(SEXP condition, String message, FunctionCall call) {
    throw new EvalException("condition signaled. message = '%s'", message);
  }
}
