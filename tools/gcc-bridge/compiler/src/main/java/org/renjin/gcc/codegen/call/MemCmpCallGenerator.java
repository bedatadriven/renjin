package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimplePointerType;


/**
 * Generates calls to memcmp() depending on the type of the arguments
 */
public class MemCmpCallGenerator implements CallGenerator {

  private final TypeOracle typeOracle;

  public MemCmpCallGenerator(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    if(call.getLhs() != null) {
      GExpr p1 = exprFactory.findGenerator(call.getOperand(0));
      GExpr p2 = exprFactory.findGenerator(call.getOperand(1));
      JExpr n = exprFactory.findPrimitiveGenerator(call.getOperand(2));

      GimplePointerType type = (GimplePointerType) call.getOperand(0).getType();
      JExpr result = typeOracle.forPointerType(type).memoryCompare(mv, p1, p2, n);

      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, new PrimitiveValue(result));
    }

  }
}
