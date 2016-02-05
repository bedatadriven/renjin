package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;


public class FatPtrFieldStrategy extends FieldStrategy {
  
  private ValueFunction valueFunction;
  private String arrayField;
  private String offsetField;
  private Type arrayType;

  public FatPtrFieldStrategy(ValueFunction valueFunction, String name) {
    this.valueFunction = valueFunction;
    this.arrayField = name;
    this.arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    this.offsetField = name + "$offset";
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayField, arrayType.getDescriptor(), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, offsetField, "I", null, null);
  }

  @Override
  public ExprGenerator memberExprGenerator(Value instance) {
    Value array = Values.field(instance, arrayType, arrayField);
    Value offset = Values.field(instance, Type.INT_TYPE, offsetField);
    return new FatPtrExpr(array, offset);
  }
}
