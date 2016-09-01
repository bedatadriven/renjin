package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for {@code void* } fields, compiled as a field of type
 * {@code java.lang.Object}
 */
public class VoidPtrField extends SingleFieldStrategy {


  public VoidPtrField(Type ownerClass, String fieldName) {
    super(ownerClass, fieldName, VoidPtrStrategy.OBJECT_TYPE);
  }

  @Override
  public VoidPtr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }
    
    FieldValue ref = new FieldValue(instance, fieldName, VoidPtrStrategy.OBJECT_TYPE);
    return new VoidPtr(ref);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr count) {
    instance.load(mv);
    mv.aconst(null);
    mv.putfield(ownerClass, fieldName, VoidPtrStrategy.OBJECT_TYPE);
  }

}
