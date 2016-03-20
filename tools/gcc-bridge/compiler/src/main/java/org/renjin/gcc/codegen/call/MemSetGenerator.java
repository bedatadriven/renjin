package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleIndirectType;

/**
 * Generates bytecode for calls to memset()
 */
public class MemSetGenerator implements CallGenerator {
  
  private final TypeOracle typeOracle;

  public MemSetGenerator(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    
    Expr pointer = exprFactory.findGenerator(call.getOperand(0));
    SimpleExpr byteValue = exprFactory.findValueGenerator(call.getOperand(1));
    SimpleExpr length = exprFactory.findValueGenerator(call.getOperand(2));

    GimpleIndirectType pointerType = (GimpleIndirectType) call.getOperand(0).getType();
    typeOracle.forPointerType(pointerType).memorySet(mv, pointer, byteValue, length);
    
    if(call.getLhs() != null) {
      LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, pointer);
    }
  }
}
