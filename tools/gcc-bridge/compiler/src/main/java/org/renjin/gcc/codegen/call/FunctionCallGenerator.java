package org.renjin.gcc.codegen.call;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.LValue;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleCall;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Generates a call to a method.
 */
public class FunctionCallGenerator implements CallGenerator {

  private final InvocationStrategy strategy;

  public FunctionCallGenerator(InvocationStrategy invocationStrategy) {
    this.strategy = invocationStrategy;
  }

  public InvocationStrategy getStrategy() {
    return strategy;
  }
  
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    // Make a list of call arguments
    List<ExprGenerator> argumentExpressions = Lists.newArrayList();
    for (GimpleExpr gimpleExpr : call.getOperands()) {
      argumentExpressions.add(exprFactory.findGenerator(gimpleExpr));
    }

    CallExpr returnValue = new CallExpr(argumentExpressions);
    
    // If we don't need the return value, then invoke and pop any result off the stack
    if(call.getLhs() == null) {
      returnValue.load(mv);
      mv.pop(returnValue.getType());
    
    } else {

      ExprGenerator callExpr = strategy.getReturnStrategy().unmarshall(mv, returnValue);
      LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
      
      lhs.store(mv, callExpr);
    }
  }
  
  private class CallExpr implements Value {

    private List<ExprGenerator> arguments;

    public CallExpr(List<ExprGenerator> arguments) {
      this.arguments = arguments;
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
        paramStrategy.emitPushParameter(mv, arguments.get(i));
      }
      // Now invoke the method
      strategy.invoke(mv);
    }
  }

}
