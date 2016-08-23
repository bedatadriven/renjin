package org.renjin.gcc.codegen.array;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrMalloc;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for array fields
 */
public class ArrayField extends SingleFieldStrategy {

  private int arrayLength;
  private final ValueFunction valueFunction;
  
  public ArrayField(Type declaringClass, String name, int arrayLength, ValueFunction valueFunction) {
    super(declaringClass, name, Wrappers.valueArrayType(valueFunction.getValueType()));
    this.arrayLength = arrayLength;
    this.valueFunction = valueFunction;
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    JExpr newArray = FatPtrMalloc.allocArray(mv, valueFunction, Expressions.constantInt(arrayLength));
    JLValue arrayField = Expressions.field(Expressions.thisValue(this.ownerClass), fieldType, fieldName);
    
    arrayField.store(mv, newArray);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {
    JExpr arrayExpr = Expressions.field(instance, fieldType, fieldName);
    JExpr offsetExpr = Expressions.constantInt(offset / 8 / valueFunction.getArrayElementBytes());
    
    if(expectedType instanceof PrimitiveTypeStrategy) {
      PrimitiveTypeStrategy primitiveTypeStrategy = (PrimitiveTypeStrategy) expectedType;
      if(!primitiveTypeStrategy.getJvmType().equals(valueFunction.getValueType())) {
        throw new InternalCompilerException("TODO: " + valueFunction.getValueType() +
            "[] => " + primitiveTypeStrategy.getType());
      }
      
      return primitiveTypeStrategy.getValueFunction().dereference(arrayExpr, offsetExpr);
    
    } else if(expectedType instanceof ArrayTypeStrategy) {
      return new ArrayExpr(valueFunction, arrayLength, arrayExpr, offsetExpr);
    
    } else {
      throw new UnsupportedOperationException("expectedType: " + expectedType);
    }
    
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    JExpr sourceArray = Expressions.field(source, fieldType, fieldName);
    JExpr destArray = Expressions.field(dest, fieldType, fieldName);
    
    mv.arrayCopy(
        sourceArray, Expressions.constantInt(0), 
        destArray, Expressions.constantInt(0), 
        Expressions.constantInt(arrayLength));
  }
}
