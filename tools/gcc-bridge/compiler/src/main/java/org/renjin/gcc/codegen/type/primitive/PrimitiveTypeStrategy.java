package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

/**
 * Strategy for dealing with primitive types.
 * 
 * <p>This is the easiest case, because there is (mostly) a one-to-one correspondence between primitive
 * types in {@code Gimple} and those of the JVM.</p>
 */
public class PrimitiveTypeStrategy implements TypeStrategy<SimpleExpr> {
  
  private GimplePrimitiveType type;

  public PrimitiveTypeStrategy(GimplePrimitiveType type) {
    this.type = type;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new SimpleParamStrategy(type.jvmType());
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new SimpleReturnStrategy(type.jvmType());
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, valueFunction());
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new SimpleFieldStrategy(type.jvmType(), fieldName);
  }

  @Override
  public SimpleExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      SimpleLValue unitArray = allocator.reserveUnitArray(decl.getName(), type.jvmType(), Optional.<SimpleExpr>absent());
      FatPtrExpr address = new FatPtrExpr(unitArray);
      SimpleExpr value = Expressions.elementAt(address.getArray(), 0);
      return new SimpleAddressableExpr(value, address);
      
    } else {
      return allocator.reserve(decl.getName(), type.jvmType());
    }
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(valueFunction());
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, valueFunction());
  }

  private ValueFunction valueFunction() {
    return new PrimitiveValueFunction(type);
  }

}
