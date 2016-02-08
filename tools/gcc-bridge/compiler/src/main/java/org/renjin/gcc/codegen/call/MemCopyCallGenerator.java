package org.renjin.gcc.codegen.call;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.var.LValue;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimplePointerType;


/**
 * Generates calls to memcpy() depending on the type of its arguments
 */
public class MemCopyCallGenerator implements CallGenerator {
  
  public static final String NAME = "__builtin_memcpy";

  private final TypeOracle typeOracle;

  public MemCopyCallGenerator(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    
    if(call.getOperands().size() != 3) {
      throw new InternalCompilerException("memcpy() expects 3 args.");
    }
    
    ExprGenerator destination = exprFactory.findGenerator(call.getOperand(0));
    ExprGenerator source =  exprFactory.findGenerator(call.getOperand(1));
    Value length = exprFactory.findValueGenerator(call.getOperand(2));

    GimplePointerType pointerType = (GimplePointerType) call.getOperand(0).getType();
    typeOracle.forType(pointerType).memoryCopy(mv, destination, source, length);
 
    if(call.getLhs() != null) {
      // memcpy() returns the destination pointer
      LValue lhs = (LValue) exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, destination);
    }
  }
}
