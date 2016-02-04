package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.runtime.*;

/**
 * Strategy for pointer types that uses a combination of an array value and an offset value
 */
public class FatPtrStrategy extends TypeStrategy<FatPtrExpr> {

  private ValueFunction valueFunction;
  private boolean parametersWrapped = true;

  /**
   * The JVM type of the array used to back the pointer
   */
  private Type arrayType; 

  public FatPtrStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
    this.arrayType = Type.getType("[" + valueFunction.getValueType().getDescriptor());
  }

  public boolean isParametersWrapped() {
    return parametersWrapped;
  }

  public FatPtrStrategy setParametersWrapped(boolean parametersWrapped) {
    this.parametersWrapped = parametersWrapped;
    return this;
  }

  @Override
  public ExprGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    Var array = allocator.reserve(decl.getName(), arrayType);
    Var offset = allocator.reserveInt(decl.getName() + "$offset");
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    return new FatPtrFieldStrategy(valueFunction, fieldName);
  }

  @Override
  public ParamStrategy getParamStrategy() {
    if(isParametersWrapped()) {
      return new WrappedFatPtrParamStrategy(valueFunction);
    } else {
      return new FatPtrParamStrategy(valueFunction);
    }
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new FatPtrReturnStrategy(valueFunction);
  }

  @Override
  public FatPtrExpr malloc(Value length) {
    return FatPtrExpr.alloc(valueFunction, length);
  }

  @Override
  public FatPtrExpr realloc(FatPtrExpr pointer, Value length) {
    Value array = new FatPtrRealloc(pointer, length);
    Value offset = Values.zero();
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public ExprGenerator valueOf(FatPtrExpr pointerExpr) {
    return valueFunction.dereference(pointerExpr.getArray(), pointerExpr.getOffset());
  }

  @Override
  public FatPtrExpr pointerPlus(FatPtrExpr pointer, Value offsetInBytes) {
    int bytesPerArrayElement = valueFunction.getElementSize() / valueFunction.getElementLength();
    Value offsetInArrayElements = Values.divide(offsetInBytes, bytesPerArrayElement);
    Value newOffset = Values.sum(pointer.getOffset(), offsetInArrayElements);
    
    return new FatPtrExpr(pointer.getArray(), newOffset);
  }

  public static Type wrapperType(Type valueType) {
    switch (valueType.getSort()) {
      case Type.BOOLEAN:
        return Type.getType(BooleanPtr.class);
      case Type.SHORT:
        return Type.getType(ShortPtr.class);
      case Type.BYTE:
        return Type.getType(BytePtr.class);
      case Type.CHAR:
        return Type.getType(CharPtr.class);
      case Type.INT:
        return Type.getType(IntPtr.class);
      case Type.LONG:
        return Type.getType(LongPtr.class);
      case Type.FLOAT:
        return Type.getType(FloatPtr.class);
      case Type.DOUBLE:
        return Type.getType(DoublePtr.class);
      case Type.OBJECT:
        return Type.getType(ObjectPtr.class);
    }
    throw new UnsupportedOperationException("No wrapper for type: " + valueType);
  }
  
  
  
}
