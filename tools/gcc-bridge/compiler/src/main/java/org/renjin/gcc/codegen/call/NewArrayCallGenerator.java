package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Implements the C++ {@code operator new[](unsigned int)} function, whose mangled name
 * is {@code _Znaj}
 */
public class NewArrayCallGenerator implements CallGenerator {

  private TypeOracle typeOracle;

  public NewArrayCallGenerator(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    // Obviously if we're not assigning this, it's a NO-OP
    if(call.getLhs() == null) {
      return;
    }

    // Generate the malloc for the given type
    GimpleType pointerType = call.getLhs().getType();

    // Find the size to allocate
    JExpr count = exprFactory.findPrimitiveGenerator(call.getOperands().get(0));

    GExpr newArray = typeOracle.forPointerType(pointerType).newArray(mv, count);
    GExpr lhs = exprFactory.findGenerator(call.getLhs());
    lhs.store(mv, newArray);
  }
}
