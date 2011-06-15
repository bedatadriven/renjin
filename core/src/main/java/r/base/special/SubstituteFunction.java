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

package r.base.special;

import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.List;

public class SubstituteFunction extends SpecialFunction {

  @Override
  public String getName() {
    return "substitute";
  }

  @Override
  public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList args) {
    checkArity(call, 2, 1);
    SEXP exp = call.getArgument(0);
    Environment environment = rho;
    if(call.getArguments().length() == 2) {
      environment =  EvalException.checkedCast(call.evalArgument(context, rho, 1));
    }

    return EvalResult.visible( substitute(exp, environment) );
  }

  private static SEXP substitute(SEXP exp, Environment environment) {
    SubstitutingVisitor visitor = new SubstitutingVisitor(environment);
    exp.accept(visitor);
    return visitor.getResult() ;
  }

  public static class SubstitutingVisitor extends SexpVisitor<SEXP> {
    private final Environment environment;
    private SEXP result;

    public SubstitutingVisitor(Environment environment) {
      this.environment = environment;
    }

    @Override
    public void visit(FunctionCall call) {
      result = new FunctionCall(
          substitute(call.getFunction()),
          substituteArgumentList(call.getArguments()),
          call.getAttributes()
      );
    }

    private PairList substituteArgumentList(PairList arguments) {
      PairList.Builder builder = PairList.Node.newBuilder();
      for(PairList.Node node : arguments.nodes()) {
        if(node.getValue().equals(Symbol.ELLIPSES)) {
          builder.addAll(unpackPromiseList((DotExp)environment.getVariable((Symbol)node.getValue())));
        } else {
          builder.add(node.getRawTag(), substitute(node.getValue()));
        }
      }
      return builder.build();
    }

    @Override
    public void visit(PairList.Node pairList) {
      PairList.Builder builder = PairList.Node.newBuilder();
      for(PairList.Node node : pairList.nodes()) {
        builder.add(node.getRawTag(), substitute(node.getValue()));
      }
      result = builder.build();
    }

    @Override
    public void visit(ListVector list) {
      ListVector.Builder builder = ListVector.newBuilder();
      for(SEXP exp : list) {
        builder.add(substitute(exp));
      }
      builder.copyAttributesFrom(list.getAttributes());
      result = builder.build();
    }

    @Override
    public void visit(ExpressionVector vector) {
      List<SEXP> list = Lists.newArrayList();
      for(SEXP exp : vector) {
        list.add( substitute(exp ));
      }
      result = new ExpressionVector(list, vector.getAttributes());
    }

    @Override
    public void visit(Symbol symbol) {
      if(environment.hasVariable(symbol)) {
        result = environment.getVariable(symbol);
        if(result instanceof Promise) {
          result = ((Promise) result).getExpression();
        }
      } else {
        result = symbol;
      }
    }

    private PairList unpackPromiseList(DotExp dotExp) {
      PairList.Builder unpacked = new PairList.Node.Builder();
      for(PairList.Node node : dotExp.getPromises().nodes()) {
        unpacked.add(node.getRawTag(), ((Promise)node.getValue()).getExpression());
      }
      return unpacked.build();
    }

    @Override
    public void visit(DotExp dotExp) {
      super.visit(dotExp);
    }

    @Override
    protected void unhandled(SEXP exp) {
      result = exp;
    }

    @Override
    public SEXP getResult() {
      return result;
    }

    private SEXP substitute(SEXP exp) {
      return SubstituteFunction.substitute(exp, environment);
    }
  }
}
