package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.param.ComplexArrayPtrParamGenerator;
import org.renjin.gcc.codegen.param.ComplexPtrParamGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ComplexReturnStrategy;
import org.renjin.gcc.codegen.ret.ReturnStrategy;
import org.renjin.gcc.codegen.var.*;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimplePointerType;

/**
 * Creates generators for complex number types.
 * 
 * <p>The JVM does not have builtin support for complex number types, so we have to
 * be a bit creative in representing parameters and variables.</p>
 *
 * <p>When used purely as value types within a function, we can simply store the complex
 * values as two {@code double} values on the stack, which should be perfectly equivalent
 * to how GCC treats them.</p>
 * 
 * <p>If we need their address, however, then they need to be allocated on the heap as a double array.</p>
 * 
 * <p>We also can't return double values, so we map functions return a complex value to one
 * returning a double array. This does require the extra step of allocating a double[] array in order
 * to return a complex value. This is something we could eliminate with aggressive inlining.</p>
 * 
 */
public class ComplexTypeFactory extends TypeFactory {

  private GimpleComplexType type;

  public ComplexTypeFactory(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public VarGenerator varGenerator(LocalVarAllocator allocator) {
    return new ComplexVarGenerator(type,
        allocator.reserve(Type.DOUBLE_TYPE),
        allocator.reserve(Type.DOUBLE_TYPE));
  }

  @Override
  public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
    return new AddressableComplexVarGenerator(type,
        allocator.reserveArrayRef());
  }

  @Override
  public ReturnStrategy returnGenerator() {
    return new ComplexReturnStrategy(type);
  }

  @Override
  public TypeFactory pointerTo() {
    return new Pointer();
  }

  @Override
  public TypeFactory arrayOf(GimpleArrayType arrayType) {
    return new Array(arrayType);
  }

  private class Pointer extends TypeFactory {

    @Override
    public ParamGenerator paramGenerator() {
      return new ComplexPtrParamGenerator(new GimplePointerType(type));
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
      return new ComplexPtrVarGenerator(type.pointerTo(), 
          allocator.reserveArrayRef(),
          allocator.reserveInt());
    }
  }
  
  private class Array extends TypeFactory {

    private final GimpleArrayType arrayType;

    public Array(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public TypeFactory pointerTo() {
      return new ArrayPtr(new GimplePointerType(arrayType));
    }
  }
  
  private class ArrayPtr extends TypeFactory {
    private final GimplePointerType pointerType;

    public ArrayPtr(GimplePointerType pointerType) {
      this.pointerType = pointerType;
    }

    @Override
    public ParamGenerator paramGenerator() {
      return new ComplexArrayPtrParamGenerator(pointerType);
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
      return new ComplexArrayPtrVarGenerator(pointerType,
          allocator.reserveArrayRef(),
          allocator.reserveInt());
    }
  }
}
