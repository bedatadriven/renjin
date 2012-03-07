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

import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.List;
import java.util.Set;

public class SubstituteFunction extends SpecialFunction {

  public SubstituteFunction() {
    super("substitute");
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    checkArity(call, 2, 1);
    SEXP exp = call.getArgument(0);
    SubstituteContext substituteContext = new EnvironmentContext(rho);
    if(call.getArguments().length() == 2) {
      SEXP envirSexp = context.evaluate(call.getArgument(1), rho);
      if(envirSexp instanceof Environment) {
        substituteContext = new EnvironmentContext((Environment) envirSexp);
      } else if(envirSexp instanceof ListVector) {
        substituteContext = new ListContext((ListVector) envirSexp);
      }
    }

    return substitute(exp, substituteContext);
  }

  private static SEXP substitute(SEXP exp, SubstituteContext context) {
    SubstitutingVisitor visitor = new SubstitutingVisitor(context);
    exp.accept(visitor);
    return visitor.getResult() ;
  }

  public static class SubstitutingVisitor extends SexpVisitor<SEXP> {
    private final SubstituteContext context;
    private SEXP result;

    public SubstitutingVisitor(SubstituteContext context) {
      this.context = context;
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
        if(node.getValue().equals(Symbols.ELLIPSES)) {
          builder.addAll(unpackPromiseList((PromisePairList)context.getVariable((Symbol)node.getValue())));
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
      builder.copyAttributesFrom(list);
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
      if(context.hasVariable(symbol)) {
        result = context.getVariable(symbol);
        if(result instanceof Promise) {
          result = ((Promise) result).getExpression();
        }
      } else {
        result = symbol;
      }
    }

    private PairList unpackPromiseList(PromisePairList dotExp) {
      PairList.Builder unpacked = new PairList.Node.Builder();
      for(PairList.Node node : dotExp.nodes()) {
        unpacked.add(node.getRawTag(), unpromise(node.getValue()));
      }
      return unpacked.build();
    }

    private SEXP unpromise(SEXP value) {
      if(value instanceof Promise) {
        return ((Promise) value).getExpression();
      } else {
        return value;
      }
    }

    @Override
    public void visit(PromisePairList dotExp) {
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
      return SubstituteFunction.substitute(exp, context);
    }
  }
  
  private interface SubstituteContext {
    SEXP getVariable(Symbol name);
    boolean hasVariable(Symbol name);
  }
  
  private static class EnvironmentContext implements SubstituteContext {
    private final Environment rho;

    public EnvironmentContext(Environment rho) {
      super();
      this.rho = rho;
    }

    @Override
    public SEXP getVariable(Symbol name) {
      return rho.getVariable(name);
    }

    @Override
    public boolean hasVariable(Symbol name) {
      return rho.hasVariable(name);
    }
  
  }
  
  private static class ListContext implements SubstituteContext {
    private ListVector list;
    
    public ListContext(ListVector list) {
      this.list = list;
    }

    @Override
    public SEXP getVariable(Symbol name) {
      int index = list.getIndexByName(name.getPrintName());
      if(index == -1) {
        return Symbol.UNBOUND_VALUE;
      } else {
        return list.getElementAsSEXP(index);
      }
    }

    @Override
    public boolean hasVariable(Symbol name) {
      return list.getIndexByName(name.getPrintName()) != -1;
    }
        
  }
  
}
