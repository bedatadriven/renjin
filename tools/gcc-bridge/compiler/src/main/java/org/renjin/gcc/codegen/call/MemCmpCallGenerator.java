package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
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
      Expr p1 = exprFactory.findGenerator(call.getOperand(0));
      Expr p2 = exprFactory.findGenerator(call.getOperand(1));
      SimpleExpr n = exprFactory.findValueGenerator(call.getOperand(2));

      GimplePointerType type = (GimplePointerType) call.getOperand(0).getType();
      SimpleExpr result = typeOracle.forPointerType(type).memoryCompare(p1, p2, n);

      LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, result);
    }

  }
}
