/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.renjin.eval.ClosureDispatcher;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.List;

public class SubstituteFunction extends SpecialFunction {

  private static final Symbol EXPR_ARGUMENT = Symbol.get("expr");
  private static final Symbol ENV_ARGUMENT = Symbol.get("env");
  
  private final PairList formals;
  
  public SubstituteFunction() {
    super("substitute");

    this.formals = new PairList.Builder()
        .add(EXPR_ARGUMENT, Symbol.MISSING_ARG)
        .add(ENV_ARGUMENT, Symbol.MISSING_ARG)
        .build();
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

    PairList matchedArguments = ClosureDispatcher.matchArguments(formals, args);

    SEXP exprArgument = matchedArguments.findByTag(EXPR_ARGUMENT);
    SEXP envArgument = matchedArguments.findByTag(ENV_ARGUMENT);
    
    // Substitute handles ... in an idiosyncratic way:
    // Only the first argument is used, and there is no attempt to 
    // match subsequent arguments against the 'env' argument.
    SEXP expr;
    if(exprArgument == Symbols.ELLIPSES) {
      
      SEXP ellipses = rho.getEllipsesVariable(Symbols.ELLIPSES);
      if(ellipses == Null.INSTANCE) {
        expr = Null.INSTANCE;
      } else {
        PromisePairList.Node promisePairList = (PromisePairList.Node) ellipses;
        Promise promisedArg = (Promise) promisePairList.getValue();
        expr = promisedArg.getExpression();
      }
    } else {
      expr = exprArgument;
    }
    
    return substitute(expr, buildContext(context, rho, envArgument));
  }

  private static SubstituteContext buildContext(Context context, Environment rho, SEXP argument) {
    if(argument == Symbol.MISSING_ARG) {
      return buildContext(context, rho);
    }
    
    SEXP env = context.evaluate(argument, rho);
    
    return buildContext(context, env);
  }
  
  private static SubstituteContext buildContext(Context context, SEXP evaluatedEnv) {
    if(evaluatedEnv instanceof Environment) {
      if(context.getGlobalEnvironment() == evaluatedEnv) {
        return new GlobalEnvironmentContext();
      } else {
        return new EnvironmentContext(context, (Environment) evaluatedEnv);
      }
    } else if(evaluatedEnv instanceof ListVector) {
      return new ListContext((ListVector) evaluatedEnv);
    } else if(evaluatedEnv instanceof PairList) {
      return new PairListContext((PairList)evaluatedEnv);
      
    } else {
      throw new EvalException("Cannot substitute using environment of type %s: expected list, pairlist, or environment", 
          evaluatedEnv.getTypeName());
    }
  }

  public static SEXP substitute(Context context, SEXP exp, SEXP environment) {
    return substitute(exp, buildContext(context, environment));
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
          SEXP extraArguments = context.getVariable(Symbols.ELLIPSES);
          if(extraArguments != Symbol.UNBOUND_VALUE) {
            builder.addAll(unpackPromiseList((PromisePairList) extraArguments));
          } else {
            builder.add(Symbols.ELLIPSES);
          }
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
        result = unpromise(context.getVariable(symbol));
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
      while(value instanceof Promise) {
        value = ((Promise) value).getExpression();
      } 
      return value;
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
    private Context context;

    public EnvironmentContext(Context context, Environment rho) {
      super();
      this.rho = rho;
      this.context = context;
    }

    @Override
    public SEXP getVariable(Symbol name) {
      return rho.getVariable(context, name);
    }

    @Override
    public boolean hasVariable(Symbol name) {
      return rho.hasVariable(name);
    }
  
  }
  
  private static class GlobalEnvironmentContext implements SubstituteContext {

    @Override
    public SEXP getVariable(Symbol name) {
      return Symbol.UNBOUND_VALUE;
    }

    @Override
    public boolean hasVariable(Symbol name) {
      return false;
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
  
  private static class PairListContext implements SubstituteContext {
    private PairList list;
    
    public PairListContext(PairList list) {
      this.list = list;
    }

    @Override
    public SEXP getVariable(Symbol name) {
      for(PairList.Node node : list.nodes()) {
        if(node.getTag() == name) {
          return node.getValue();
        }
      }
      return Symbol.UNBOUND_VALUE;
    }

    @Override
    public boolean hasVariable(Symbol name) {
      return getVariable(name) != Symbol.UNBOUND_VALUE;
    }
        
  }
}
