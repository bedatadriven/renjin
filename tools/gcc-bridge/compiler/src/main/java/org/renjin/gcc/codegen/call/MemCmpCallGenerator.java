package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.var.LValue;
import org.renjin.gcc.codegen.var.Value;
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
      ExprGenerator p1 = exprFactory.findGenerator(call.getOperand(0));
      ExprGenerator p2 = exprFactory.findGenerator(call.getOperand(1));
      Value n = exprFactory.findValueGenerator(call.getOperand(2));

      GimplePointerType type = (GimplePointerType) call.getOperand(0).getType();
      Value result = typeOracle.forType(type).memoryCompare(p1, p2, n);

      LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, result);
    }

  }
}
