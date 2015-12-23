package org.renjin.gcc.codegen.type;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.VarAllocator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.arrays.PrimitiveArrayConstructor;
import org.renjin.gcc.codegen.arrays.PrimitivePtrArrayConstructor;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.field.*;
import org.renjin.gcc.codegen.param.ParamStrategy;
import org.renjin.gcc.codegen.param.PrimitiveParamStrategy;
import org.renjin.gcc.codegen.param.PrimitivePtrParamStrategy;
import org.renjin.gcc.codegen.param.PrimitivePtrPtrParamStrategy;
import org.renjin.gcc.codegen.ret.PrimitivePtrPtrReturnStrategy;
import org.renjin.gcc.codegen.ret.PrimitivePtrReturnStrategy;
import org.renjin.gcc.codegen.ret.PrimitiveReturnStrategy;
import org.renjin.gcc.codegen.ret.ReturnStrategy;
import org.renjin.gcc.codegen.var.*;
import org.renjin.gcc.gimple.GimpleVarDecl;
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
public class PrimitiveTypeStrategy extends TypeStrategy {
  
  private GimplePrimitiveType type;

  public PrimitiveTypeStrategy(GimplePrimitiveType type) {
    this.type = type;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new PrimitiveParamStrategy(type);
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new PrimitiveReturnStrategy(type);
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
  public TypeStrategy pointerTo() {
    return new Pointer();
  }

  @Override
  public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      return new AddressablePrimitiveVarGenerator(type, 
          allocator.reserveArrayRef(decl.getName(), type.jvmType()));
    } else {
      return new PrimitiveVarGenerator(type, 
          allocator.reserve(decl.getName(), type.jvmType()));
    }
  }

  @Override
  public TypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new Array(arrayType);
  }

  private class Pointer extends TypeStrategy {
    
    private GimplePointerType pointerType = new GimplePointerType(type);

    @Override
    public ParamStrategy getParamStrategy() {
      return new PrimitivePtrParamStrategy(pointerType);
    }

    @Override
    public ReturnStrategy getReturnStrategy() {
      return new PrimitivePtrReturnStrategy(new GimplePointerType(type));
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      if(decl.isAddressable()) {
        return new AddressablePrimitivePtrVar(pointerType,
            allocator.reserveArrayRef(decl.getName(), WrapperType.wrapperType(type)));
      } else {
        return new PrimitivePtrVarGenerator(pointerType,
            allocator.reserveArrayRef(decl.getName(), type.jvmType()),
            allocator.reserve(decl.getName() + "$offset", Type.INT_TYPE));
      }
    }

    @Override
    public TypeStrategy pointerTo() {
      return new PointerPointer(new GimplePointerType(pointerType));
    }

    @Override
    public TypeStrategy arrayOf(GimpleArrayType arrayType) {
      return new PointerArray(arrayType);
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new PrimitivePtrFieldGenerator(className, fieldName, pointerType);
    }

    @Override
    public FieldGenerator addressableFieldGenerator(String className, String fieldName) {
      return new AddressablePrimitivePtrField(className, fieldName, pointerType);
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      return new MallocGenerator(type.pointerTo(), type.jvmType(), pointerType.getBaseType().sizeOf(), size);
    }
  }

  /**
   * Pointer to a pointer to a primitive value or array
   */
  private class PointerPointer extends TypeStrategy {
    
    private GimpleIndirectType pointerType;

    public PointerPointer(GimpleIndirectType pointerType) {
      this.pointerType = pointerType;
    }

    @Override
    public ParamStrategy getParamStrategy() {
      return new PrimitivePtrPtrParamStrategy(pointerType);
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new PrimitivePtrPtrFieldGenerator(className, fieldName, pointerType);
    }
    @Override
    public ReturnStrategy getReturnStrategy() {
      return new PrimitivePtrPtrReturnStrategy(pointerType);
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return new PrimitivePtrPtrVarGenerator(pointerType, 
          allocator.reserveArrayRef(decl.getName(), WrapperType.of(type).getWrapperType()), 
          allocator.reserveInt(decl.getName() + "$offset"));
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      return new MallocGenerator(pointerType, WrapperType.of(type).getWrapperType(), pointerType.getBaseType().sizeOf(), size);
    }
  }

  /**
   * Array of primitives
   */
  private class Array extends TypeStrategy {

    private final GimpleArrayType arrayType;

    public Array(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public TypeStrategy pointerTo() {
      return new ArrayPtr(new GimplePointerType(arrayType));
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return new PrimitiveArrayVar(arrayType, 
          allocator.reserveArrayRef(decl.getName(), type.jvmType()));
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new PrimitiveArrayFieldGenerator(className, fieldName, arrayType);
    }
    
    @Override
    public FieldGenerator addressableFieldGenerator(String className, String fieldName) {
      return fieldGenerator(className, fieldName);
    }

    @Override
    public ExprGenerator constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
      List<ExprGenerator> elements = Lists.newArrayList();
      for (GimpleConstructor.Element element : value.getElements()) {
        elements.add(exprFactory.findGenerator(element.getValue()));
      }
      
      return new PrimitiveArrayConstructor(arrayType, elements);
    }
  }
  
  private class ArrayPtr extends TypeStrategy {
    private GimplePointerType arrayPtrType;

    public ArrayPtr(GimplePointerType arrayPtrType) {
      this.arrayPtrType = arrayPtrType;
    }

    @Override
    public ParamStrategy getParamStrategy() {
      // A pointer to an array of primitives is essentially the same thing as
      // a pointer to a single primitive value, the only difference is that the memory
      // region to which the parameter points is longer than a single value...
      return new PrimitivePtrParamStrategy(arrayPtrType);
    }

    @Override
    public TypeStrategy pointerTo() {
      return new PointerPointer(arrayPtrType);
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return new PrimitiveArrayPtrVar(arrayPtrType, 
          allocator.reserveArrayRef(decl.getName(), type.jvmType()), 
          allocator.reserveInt(decl.getName() + "$offset"));
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      return new MallocGenerator(arrayPtrType,  type.jvmType(), type.sizeOf(), size);
    }
  }

  private class PointerArray extends TypeStrategy {
    private GimpleArrayType arrayType;

    public PointerArray(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return new PrimitivePtrArrayVar(arrayType, allocator.reserveArrayRef(decl.getName(), 
          WrapperType.of(arrayType.getComponentType()).getWrapperType()));
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
