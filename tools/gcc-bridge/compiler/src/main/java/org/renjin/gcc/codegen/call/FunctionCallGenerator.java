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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.fun.FunctionRefGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Lists;

import javax.annotation.Nonnull;
import java.util.List;

import static org.renjin.gcc.codegen.expr.Expressions.box;

/**
 * Generates a call to a method.
 */
public class FunctionCallGenerator implements CallGenerator, MethodHandleGenerator {

  private final InvocationStrategy strategy;

  public FunctionCallGenerator(InvocationStrategy invocationStrategy) {
    this.strategy = invocationStrategy;
  }

  public InvocationStrategy getStrategy() {
    return strategy;
  }
  
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {


    // The number of fixed (gimple) parameters expected, excluding var args
    // the number of Jvm arguments may be greater
    int fixedArgCount = strategy.getParamStrategies().size();
    
    // Make a list of (fixed) call arguments
    List<GExpr> argumentExpressions = Lists.newArrayList();
    for (int i = 0; i < fixedArgCount; i++) {
      if(i < call.getOperands().size()) {
        argumentExpressions.add(exprFactory.findGenerator(call.getOperand(i)));
      }
    }

    // if this method accepts var args, then we pass the 
    // remaining arguments as an Object[] array
    Optional<JExpr> varArgArray = Optional.absent();
    if(strategy.isVarArgs()) {
      List<JExpr> varArgValues = Lists.newArrayList();
      for(int i=fixedArgCount;i<call.getOperands().size(); ++i) {
        GExpr varArgExpr = exprFactory.findGenerator(call.getOperand(i));
        varArgValues.add(wrapVarArg(varArgExpr));
      }
      varArgArray = Optional.of(Expressions.newArray(Type.getType(Object.class), varArgValues));
    }
    
    CallExpr callExpr = new CallExpr(argumentExpressions, varArgArray);
    
    // If we don't need the return value, then invoke and pop any result off the stack
    if(call.getLhs() == null) {
      callExpr.load(mv);
      mv.pop(callExpr.getType());
    
    } else {

      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      TypeStrategy lhsTypeStrategy = exprFactory.strategyFor(call.getLhs().getType());
      GExpr rhs = strategy.getReturnStrategy().unmarshall(mv, callExpr, lhsTypeStrategy);

      lhs.store(mv, rhs);
    }
  }

  private JExpr wrapVarArg(GExpr varArgExpr) {
    // TODO: generalize
    // This is quite specific to printf()
    if(varArgExpr instanceof PrimitiveValue) {
      return box(((PrimitiveValue) varArgExpr).unwrap());

    } else if(varArgExpr instanceof GSimpleExpr) {
      return ((GSimpleExpr) varArgExpr).unwrap();

    } else if(varArgExpr instanceof PtrExpr) {
      return varArgExpr.toVPtrExpr().getRef();

    } else {
      throw new UnsupportedOperationException("varArgExpr: " + varArgExpr);
    }
  }

  @Override
  public JExpr getMethodHandle() {
    return new FunctionRefGenerator(strategy.getMethodHandle());
  }

  private class CallExpr implements JExpr {

    private List<GExpr> arguments;
    private Optional<JExpr> varArgArray;

    public CallExpr(List<GExpr> arguments, Optional<JExpr> varArgArray) {
      this.arguments = arguments;
      this.varArgArray = varArgArray;
    }

    @Nonnull
    @Override
    public Type getType() {
      return strategy.getReturnStrategy().getType();
    }

    @Override
    public void load(@Nonnull MethodGenerator mv) {
      // Push all parameters on the stack
      List<ParamStrategy> paramStrategies = strategy.getParamStrategies();
      for (int i = 0; i < paramStrategies.size(); i++) {
        ParamStrategy paramStrategy = paramStrategies.get(i);
        if(i < arguments.size()) {
          paramStrategy.loadParameter(mv, Optional.of(arguments.get(i)));
        } else {
          paramStrategy.loadParameter(mv, Optional.<GExpr>absent());
        }
      }
      if(varArgArray.isPresent()) {
        varArgArray.get().load(mv);
      }
      
      // Now invoke the method
      strategy.invoke(mv);
    }
  }

}
