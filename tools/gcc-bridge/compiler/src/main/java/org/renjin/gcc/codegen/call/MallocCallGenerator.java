package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates function calls to {@code malloc()}
 */
public class MallocCallGenerator implements CallGenerator {
  
  private TypeOracle typeOracle;

  public MallocCallGenerator(TypeOracle typeOracle) {
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
    SimpleExpr size = exprFactory.findValueGenerator(call.getOperands().get(0));
    
    Expr mallocGenerator = typeOracle.forPointerType(pointerType).malloc(mv, size);
    LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
    lhs.store(mv, mallocGenerator);
  }
}
