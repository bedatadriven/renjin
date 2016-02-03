package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimplePointerType;

/**
 * Strategy  complex number types.
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
public class ComplexTypeStrategy extends TypeStrategy {

  private GimpleComplexType type;

  public ComplexTypeStrategy(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public ExprGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
//      return new AddressableComplexVarGenerator(type,
//          allocator.reserveArrayRef(decl.getName(), type.getJvmPartType()));
      throw new UnsupportedOperationException("TODO");
    } else {
      return new ComplexValue(
          allocator.reserve(decl.getName() + "$real", type.getJvmPartType()),
          allocator.reserve(decl.getName() + "$im", type.getJvmPartType()));
    }
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new ComplexReturnStrategy(type);
  }

  @Override
  public TypeStrategy pointerTo() {
    return new Pointer();
  }

  @Override
  public TypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new Array(arrayType);
  }

  private class Pointer extends TypeStrategy {

    @Override
    public ParamStrategy getParamStrategy() {
      //return new ComplexPtrParamStrategy(new GimplePointerType(type));
      throw new UnsupportedOperationException();
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
//      return new ComplexPtrVarGenerator(type.pointerTo(), 
//          allocator.reserveArrayRef(decl.getName(), type.getJvmPartType()),
//          allocator.reserveInt(decl.getName() + "$offset"));
      throw new UnsupportedOperationException("TODO");
    }
  }
  
  private class Array extends TypeStrategy {

    private final GimpleArrayType arrayType;

    public Array(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public TypeStrategy pointerTo() {
      return new ArrayPtr(new GimplePointerType(arrayType));
    }
  }
  
  private class ArrayPtr extends TypeStrategy {
    private final GimplePointerType pointerType;

    public ArrayPtr(GimplePointerType pointerType) {
      this.pointerType = pointerType;
    }
    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
//      return new ComplexArrayPtrVarGenerator(pointerType,
//          allocator.reserveArrayRef(decl.getName(), type.getJvmPartType()),
//          allocator.reserveInt(decl.getName() + "$offset"));
      throw new UnsupportedOperationException();
    }
  }
}
