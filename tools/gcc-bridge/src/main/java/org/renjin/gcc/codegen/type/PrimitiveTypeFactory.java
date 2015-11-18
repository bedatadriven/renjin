package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.field.*;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.PrimitiveParamGenerator;
import org.renjin.gcc.codegen.param.PrimitivePtrParamGenerator;
import org.renjin.gcc.codegen.param.PrimitivePtrPtrParamGenerator;
import org.renjin.gcc.codegen.ret.PrimitivePtrPtrReturnGenerator;
import org.renjin.gcc.codegen.ret.PrimitivePtrReturnGenerator;
import org.renjin.gcc.codegen.ret.PrimitiveReturnGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.var.*;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@code Generators} for {@code GimplePrimitiveType}.
 * 
 * <p>This is the easiest case, because there is a one-to-one correspondence between primitive
 * types in {@code Gimple} and on the JVM.</p>
 */
public class PrimitiveTypeFactory extends TypeFactory {
  
  private GimplePrimitiveType type;

  public PrimitiveTypeFactory(GimplePrimitiveType type) {
    this.type = type;
  }

  @Override
  public ParamGenerator paramGenerator() {
    return new PrimitiveParamGenerator(type);
  }

  @Override
  public ReturnGenerator returnGenerator() {
    return new PrimitiveReturnGenerator(type);
  }

  @Override
  public FieldGenerator addressableFieldGenerator(String className, String fieldName) {
    return new AddressablePrimitiveField(className, fieldName, type, type.jvmType());
  }

  @Override
  public FieldGenerator fieldGenerator(String className, String fieldName) {
    return new PrimitiveFieldGenerator(className, fieldName, type, type.jvmType());
  }

  @Override
  public TypeFactory pointerTo() {
    return new Pointer();
  }

  @Override
  public VarGenerator varGenerator(LocalVarAllocator allocator) {
    return new PrimitiveVarGenerator(type, allocator.reserve(type.jvmType()));
  }

  @Override
  public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
    return new AddressablePrimitiveVarGenerator(type, allocator.reserveArrayRef());
  }

  @Override
  public TypeFactory arrayOf(GimpleArrayType arrayType) {
    return new Array(arrayType);
  }

  private class Pointer extends TypeFactory {
    
    private GimplePointerType pointerType = new GimplePointerType(type);

    @Override
    public ParamGenerator paramGenerator() {
      return new PrimitivePtrParamGenerator(pointerType);
    }

    @Override
    public ReturnGenerator returnGenerator() {
      return new PrimitivePtrReturnGenerator(new GimplePointerType(type));
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
      return new PrimitivePtrVarGenerator(pointerType,
          allocator.reserveArrayRef(),
          allocator.reserve(Type.INT_TYPE));
    }

    @Override
    public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
      return new AddressablePrimitivePtrVar(pointerType,
          allocator.reserveObject());
    }

    @Override
    public TypeFactory pointerTo() {
      return new PointerPointer(new GimplePointerType(pointerType));
    }

    @Override
    public TypeFactory arrayOf(GimpleArrayType arrayType) {
      return new PointerArray(arrayType);
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new PrimitivePtrFieldGenerator(className, fieldName, pointerType);
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      return new MallocGenerator(type.jvmType(), pointerType.getBaseType().sizeOf(), size);
    }
  }

  /**
   * Pointer to a pointer to a primitive value or array
   */
  private class PointerPointer extends TypeFactory {
    
    private GimpleIndirectType pointerType;

    public PointerPointer(GimpleIndirectType pointerType) {
      this.pointerType = pointerType;
    }

    @Override
    public ParamGenerator paramGenerator() {
      return new PrimitivePtrPtrParamGenerator(pointerType);
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new PrimitivePtrPtrFieldGenerator(className, fieldName, pointerType);
    }

    @Override
    public ReturnGenerator returnGenerator() {
      return new PrimitivePtrPtrReturnGenerator(pointerType);
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
      return new PrimitivePtrPtrVarGenerator(pointerType, allocator.reserveObject(), allocator.reserveInt());
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      return new MallocGenerator(WrapperType.of(type).getWrapperType(), pointerType.getBaseType().sizeOf(), size);
    }
  }

  /**
   * Array of primitives
   */
  private class Array extends TypeFactory {

    private final GimpleArrayType arrayType;

    public Array(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public TypeFactory pointerTo() {
      return new ArrayPtr(new GimplePointerType(arrayType));
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
      return new ArrayVarGenerator(arrayType, allocator.reserveArrayRef());
    }

    @Override
    public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
      return varGenerator(allocator);
    }
  }
  
  private class ArrayPtr extends TypeFactory {
    private GimplePointerType arrayPtrType;

    public ArrayPtr(GimplePointerType arrayPtrType) {
      this.arrayPtrType = arrayPtrType;
    }

    @Override
    public ParamGenerator paramGenerator() {
      // A pointer to an array of primitives is essentially the same thing as
      // a pointer to a single primitive value, the only difference is that the memory
      // region to which the parameter points is longer than a single value...
      return new PrimitivePtrParamGenerator(arrayPtrType);
    }

    @Override
    public TypeFactory pointerTo() {
      return new PointerPointer(arrayPtrType);
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
      return new ArrayPtrVarGenerator(arrayPtrType, 
          allocator.reserveArrayRef(), 
          allocator.reserveInt());
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      return new MallocGenerator(type.jvmType(), type.sizeOf(), size);
    }
  }

  private class PointerArray extends TypeFactory {
    private GimpleArrayType arrayType;

    public PointerArray(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
      return new PrimitivePtrArrayVar(arrayType, allocator.reserveObject());
    }

    @Override
    public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
      return varGenerator(allocator);
    }

    @Override
    public ExprGenerator constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
      
      List<ExprGenerator> elements = new ArrayList<>();
      for (GimpleConstructor.Element element : value.getElements()) {
        elements.add(exprFactory.findGenerator(element.getValue()));
      }
      return new PrimitivePtrArrayConstructor(arrayType, elements);
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new PrimitivePtrArrayField(className, fieldName, arrayType);
    }
  }
}
