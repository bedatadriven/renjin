package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValueFunction;
import org.renjin.gcc.codegen.type.voidt.VoidPtrValueFunction;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

/**
 * A field which can point to any primitive type.
 */
public class PrimitivePointerUnionField extends FieldStrategy {

  private Type declaringClass;
  private String name;

  public PrimitivePointerUnionField(Type declaringClass, String name) {
    this.declaringClass = declaringClass;
    this.name = name;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, Type.getDescriptor(Object.class), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, name + "$offset", Type.INT_TYPE.getDescriptor(), null, null);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int fieldOffset, TypeStrategy expectedType) {
    JLValue arrayExpr = Expressions.field(instance, Type.getType(Object.class), name);
    JLValue offsetExpr = Expressions.field(instance, Type.INT_TYPE, name + "$offset");

    if(expectedType == null) {
      return new FatPtrPair(new VoidPtrValueFunction(), arrayExpr, offsetExpr);

    } else if(expectedType instanceof FatPtrStrategy) {
      ValueFunction valueFunction = ((FatPtrStrategy) expectedType).getValueFunction();
      if(valueFunction instanceof PrimitiveValueFunction) {
        Type baseType = valueFunction.getValueType();
        Type expectedArrayType = Wrappers.valueArrayType(baseType);
        JExpr castedArrayExpr = Expressions.cast(arrayExpr, expectedArrayType);

        return new FatPtrPair(new PrimitiveValueFunction(baseType), castedArrayExpr, offsetExpr);
      }
    } 
    throw new UnsupportedOperationException("Type: " + expectedType);
  }
}
