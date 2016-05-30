package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.op.CastGenerator;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.guava.base.Optional;

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

  public GimplePrimitiveType getType() {
    return type;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new PrimitiveParamStrategy(type.jvmType());
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
    return new SimpleFieldStrategy(fieldName, type.jvmType());
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

  @Override
  public SimpleExpr cast(Expr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    
    if(typeStrategy instanceof PrimitiveTypeStrategy) {
      // Handle casts between primitive types and signed/unsigned
      GimplePrimitiveType valueType = ((PrimitiveTypeStrategy) typeStrategy).getType();
      return new CastGenerator((SimpleExpr) value, valueType, this.type);
    }
    
    if(typeStrategy instanceof FatPtrStrategy) {
      // Converting pointers to integers and vice-versa is implementation-defined
      // So we will define an implementation that supports at least one useful case spotted in S4Vectors:
      // double a[] = {1,2,3,4};
      // double *start = a;
      // double *end = p+4;
      // int length = (start-end)
      FatPtrExpr fatPtr = (FatPtrExpr) value;
      return fatPtr.getOffset();
    
    } else if(typeStrategy instanceof RecordUnitPtrStrategy) {
      return Expressions.identityHash((SimpleExpr)value);
    }
    
    throw new UnsupportedCastException();
  }
  
  public SimpleExpr zero() {
    return new ConstantValue(type.jvmType(), 0);
  }

  private ValueFunction valueFunction() {
    return new PrimitiveValueFunction(type);
  }

}
