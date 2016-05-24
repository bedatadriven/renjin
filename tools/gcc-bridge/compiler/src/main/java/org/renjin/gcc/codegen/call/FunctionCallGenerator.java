package org.renjin.gcc.codegen.call;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.fun.FunctionRefGenerator;
import org.renjin.gcc.gimple.statement.GimpleCall;

import javax.annotation.Nonnull;
import java.util.List;

import static org.renjin.gcc.codegen.expr.Expressions.box;
import static org.renjin.gcc.codegen.expr.Expressions.isPrimitive;

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
    if(call.getOperands().size() < fixedArgCount) {
      throw new InternalCompilerException(String.format("Number of provided arguments (%d) does not match " +
          "number of expected arguments (%d) for " + strategy,
          call.getOperands().size(),
          fixedArgCount));
    }
    
    // Make a list of (fixed) call arguments
    List<Expr> argumentExpressions = Lists.newArrayList();
    for (int i = 0; i < fixedArgCount; i++) {
      argumentExpressions.add(exprFactory.findGenerator(call.getOperand(i)));
    }

    // if this method accepts var args, then we pass the 
    // remaining arguments as an Object[] array
    Optional<SimpleExpr> varArgArray = Optional.absent();
    if(strategy.isVarArgs()) {
      List<SimpleExpr> varArgValues = Lists.newArrayList();
      for(int i=fixedArgCount;i<call.getOperands().size(); ++i) {
        Expr varArgExpr = exprFactory.findGenerator(call.getOperand(i));
        varArgValues.add(wrapVarArg(varArgExpr));
      }
      varArgArray = Optional.of(Expressions.newArray(Type.getType(Object.class), varArgValues));
    }
    
    CallExpr returnValue = new CallExpr(argumentExpressions, varArgArray);
    
    // If we don't need the return value, then invoke and pop any result off the stack
    if(call.getLhs() == null) {
      returnValue.load(mv);
      mv.pop(returnValue.getType());
    
    } else {

      LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
      TypeStrategy lhsTypeStrategy = exprFactory.strategyFor(call.getLhs().getType());
      Expr rhs = strategy.getReturnStrategy().unmarshall(mv, returnValue, lhsTypeStrategy);

      lhs.store(mv, rhs);
    }
  }

  private SimpleExpr wrapVarArg(Expr varArgExpr) {
    // TODO: generalize
    // This is quite specific to printf()
    if(varArgExpr instanceof SimpleExpr) {
      SimpleExpr simpleExpr = (SimpleExpr) varArgExpr;
      if(isPrimitive(simpleExpr)) {
        return box(simpleExpr);
      } else {
        return simpleExpr;
      }
    } else if(varArgExpr instanceof FatPtrExpr) {
      return ((FatPtrExpr) varArgExpr).wrap();
    } else {
      throw new UnsupportedOperationException("varArgExpr: " + varArgExpr);
    }
  }

  @Override
  public SimpleExpr getMethodHandle() {
    return new FunctionRefGenerator(strategy.getMethodHandle());
  }

  private class CallExpr implements SimpleExpr {

    private List<Expr> arguments;
    private Optional<SimpleExpr> varArgArray;

    public CallExpr(List<Expr> arguments, Optional<SimpleExpr> varArgArray) {
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
        paramStrategy.loadParameter(mv, arguments.get(i));
      }
      if(varArgArray.isPresent()) {
        varArgArray.get().load(mv);
      }
      
      // Now invoke the method
      strategy.invoke(mv);
    }
  }

}
