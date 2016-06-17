package org.renjin.gcc.codegen.type.complex;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleComplexType;

/**
 * Strategy for complex number types.
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
public class ComplexTypeStrategy implements TypeStrategy<ComplexValue> {

  private GimpleComplexType type;

  public ComplexTypeStrategy(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public ComplexValue variable(GimpleVarDecl decl, VarAllocator allocator) {
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
  public ComplexValue constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ParamStrategy getParamStrategy() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new ComplexReturnStrategy(type);
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(new ComplexValueFunction(type.getJvmPartType()))
        .setParametersWrapped(false);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, new ComplexValueFunction(type.getJvmPartType()))
        .setParameterWrapped(false);
  }

  @Override
  public ComplexValue cast(GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }
}
