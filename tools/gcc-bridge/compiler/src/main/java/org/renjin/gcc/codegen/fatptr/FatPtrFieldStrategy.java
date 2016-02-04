package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.var.Value;


public class FatPtrFieldStrategy extends FieldStrategy {
  
  private ValueFunction valueFunction;
  private String arrayField;
  private String offsetField;
  
  public FatPtrFieldStrategy(ValueFunction valueFunction, String name) {
    this.valueFunction = valueFunction;
    this.arrayField = name;
    this.offsetField = name + "$offset";
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayField, 
        Wrappers.valueArrayType(valueFunction.getValueType()).getDescriptor(),
        null, null);

    cv.visitField(Opcodes.ACC_PUBLIC, offsetField, "I", null, null);
  }

  @Override
  public ExprGenerator memberExprGenerator(Value instance) {
    Value array = Wrappers.getArray(instance);
    Value offset = Wrappers.getOffset(instance);
    return new FatPtrExpr(array, offset);
  }
}
