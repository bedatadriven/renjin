package org.renjin.gcc.codegen.call;

import com.google.common.base.Preconditions;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.gimple.statement.GimpleCall;

/**
 * Count leading zeros of an integer
 */
public class BuiltinClzGenerator implements CallGenerator {
  
  public static final String NAME = "__builtin_clz";
  
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    Preconditions.checkArgument(call.getOperands().size() == 1, "Expected 1 argument");
    
    // If we are not assigning the result, this is is a NO-OP
    if(call.getLhs() == null) {
      return;
    }
    GExpr lhs = exprFactory.findGenerator(call.getLhs());
    
    
    GExpr value = exprFactory.findGenerator(call.getOperand(0));
    if(!(value instanceof PrimitiveValue)) {
      throw new InternalCompilerException("Expected primitive operand: " + value);
    }
    
    PrimitiveValue argument = (PrimitiveValue) value;
    PrimitiveValue result = new PrimitiveValue(Expressions.numberOfLeadingZeros(argument.unwrap()));
    
    lhs.store(mv, result);
  }
}
