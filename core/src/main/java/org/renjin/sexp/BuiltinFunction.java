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
package org.renjin.sexp;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;

import java.util.ArrayList;
import java.util.List;

public abstract class BuiltinFunction extends PrimitiveFunction {

  public static final String TYPE_NAME = "builtin";
  public static final String IMPLICIT_CLASS = "function";
  
  private final String name;

  public BuiltinFunction(String name) {
    this.name = name;
  }
  
  @Override
  public String getName() {
    return name;
  }

  private boolean isEmptyArgumentAllowed() {
    return name.charAt(0) == '[' || name.equals("rep");
  }

  @Override
  public final String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public final String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  @Override
  public final void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public final SEXP apply(Context context, Environment rho, FunctionCall call) {
    List<String> argumentNames = new ArrayList<>();
    List<SEXP> arguments = new ArrayList<>();

    for (PairList.Node node : call.getArguments().nodes()) {
      SEXP value = node.getValue();
      if(value == Symbols.ELLIPSES) {
        SEXP expando = rho.getEllipsesVariable();
        if(expando == Symbol.UNBOUND_VALUE) {
          throw new EvalException("'...' used in an incorrect context");
        }
        if(expando instanceof PromisePairList) {
          PromisePairList extra = (PromisePairList) expando;
          for (PairList.Node extraNode : extra.nodes()) {
            argumentNames.add(extraNode.hasTag() ? extraNode.getName() : null);
            arguments.add(extraNode.getValue());
          }
        }

      } else {
        if(node.hasName()) {
          argumentNames.add(node.getTag().getPrintName());
        } else {
          argumentNames.add(null);
        }
        if(value == Symbol.MISSING_ARG) {
          if(isEmptyArgumentAllowed()) {
            arguments.add(Symbol.MISSING_ARG);
          } else {
            int argumentNumber = arguments.size() + 1;
            throw new EvalException("argument " + argumentNumber + " is empty");
          }
        } else {
          arguments.add(value.promise(rho));
        }
      }
    }
    return applyPromised(context, rho, call, argumentNames.toArray(new String[0]), arguments.toArray(new SEXP[0]), null);
  }

}
