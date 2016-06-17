package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
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

    
    GExpr pointer = exprFactory.findGenerator(call.getOperand(0));
    JExpr byteValue = exprFactory.findPrimitiveGenerator(call.getOperand(1));
    JExpr length = exprFactory.findPrimitiveGenerator(call.getOperand(2));

    GimpleIndirectType pointerType = (GimpleIndirectType) call.getOperand(0).getType();
    typeOracle.forPointerType(pointerType).memorySet(mv, pointer, byteValue, length);
    
    if(call.getLhs() != null) {
      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, pointer);
    }
  }
}
