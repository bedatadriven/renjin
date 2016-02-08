package org.renjin.gcc.codegen.array;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrMalloc;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;

/**
 * Strategy for array fields
 */
public class ArrayField extends FieldStrategy {

  private Type declaringClass;
  private String name;
  private int arrayLength;
  private final ValueFunction valueFunction;
  private final Type arrayType;

  public ArrayField(Type declaringClass, String name, int arrayLength, ValueFunction valueFunction) {
    this.declaringClass = declaringClass;
    this.name = name;
    this.arrayLength = arrayLength;
    this.valueFunction = valueFunction;
    this.arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, arrayType.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    SimpleExpr newArray = FatPtrMalloc.allocArray(mv, valueFunction, Expressions.constantInt(arrayLength));
    LValue arrayField = Expressions.field(Expressions.thisValue(declaringClass), arrayType, name);
    
    arrayField.store(mv, newArray);
  }

  @Override
  public Expr memberExprGenerator(SimpleExpr instance) {
    SimpleExpr array = Expressions.field(instance, arrayType, name);
    SimpleExpr offset = Expressions.zero();
    FatPtrExpr address = new FatPtrExpr(array, offset);
    return new FatPtrExpr(address, array, offset);
  }
}
