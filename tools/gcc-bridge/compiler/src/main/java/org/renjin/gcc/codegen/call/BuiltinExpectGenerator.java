package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.primitive.op.ConditionExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.statement.GimpleCall;

/**
 * The __builtin_expect() function allows the programmer to provide branch prediction information.
 * We ignore it presently and just check that the two integral arguments are equal
 */
public class BuiltinExpectGenerator implements CallGenerator {
  
  public static final String NAME = "__builtin_expect";
  
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    ConditionGenerator conditionGenerator = exprFactory.findConditionGenerator(GimpleOp.NE_EXPR, call.getOperands());
    ConditionExpr conditionExpr = new ConditionExpr(conditionGenerator);
    
    PrimitiveValue lhs = (PrimitiveValue) exprFactory.findGenerator(call.getLhs());
    lhs.store(mv, new PrimitiveValue(conditionExpr));
  }
}
