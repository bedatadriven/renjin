package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.var.LValue;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates calls to realloc().
 *
 * <p>The C library function void *realloc(void *ptr, size_t size) attempts to resize the memory block pointed to
 * by ptr that was previously allocated with a call to malloc or calloc. .</p>
 */
public class ReallocCallGenerator implements CallGenerator {

  private TypeOracle typeOracle;

  public ReallocCallGenerator(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    // If the return result is not used, then it's a no-op
    if(call.getLhs() == null) {
      return;
    }

    // Get the type of the variable we're assigning to
    GimpleType pointerType = call.getLhs().getType();

    // Get generators for the fat pointer and new length
    ExprGenerator pointer = exprFactory.findGenerator(call.getOperand(0));
    Value size = exprFactory.findValueGenerator(call.getOperand(1));
    Value length = Values.divide(size, pointerType.getBaseType().sizeOf());

    ExprGenerator reallocatedPointer = typeOracle.forType(pointerType).realloc(pointer, length);

    LValue lhs = (LValue)exprFactory.findGenerator(call.getLhs());
    lhs.store(mv, reallocatedPointer);
  }
}
