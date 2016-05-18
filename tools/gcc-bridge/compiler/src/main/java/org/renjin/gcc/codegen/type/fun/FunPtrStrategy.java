package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.type.record.unit.RefConditionGenerator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

import java.lang.invoke.MethodHandle;

/**
 * Strategy for function pointer types
 */
public class FunPtrStrategy implements PointerTypeStrategy<SimpleExpr> {
  
  public static final Type METHOD_HANDLE_TYPE = Type.getType(MethodHandle.class);

  public FunPtrStrategy() {
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new SimpleParamStrategy(METHOD_HANDLE_TYPE);
  }

  @Override
  public SimpleLValue variable(GimpleVarDecl decl, VarAllocator allocator) {
    return allocator.reserve(decl.getName(), Type.getType(MethodHandle.class));
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new SimpleFieldStrategy(fieldName, METHOD_HANDLE_TYPE);
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new SimpleReturnStrategy(METHOD_HANDLE_TYPE);
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(new FunPtrValueFunction(32));
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, new FunPtrValueFunction(32));
  }

  @Override
  public SimpleExpr cast(Expr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof FunPtrStrategy) {
      // We can liberally cast between different types of function pointers thanks
      // to the flexibility of MethodHandles.
      return (SimpleExpr) value;
    }
    
    // TODO: remove this, just to get rtti running
    if(typeStrategy instanceof RecordUnitPtrStrategy) {
      return nullPointer();
    }
    
    if(typeStrategy instanceof PrimitiveTypeStrategy) {
      return nullPointer();
    }
    
    throw new UnsupportedCastException();
  }

  @Override
  public SimpleExpr nullPointer() {
    return Expressions.nullRef(METHOD_HANDLE_TYPE);
  }

  @Override
  public ConditionGenerator comparePointers(GimpleOp op, SimpleExpr x, SimpleExpr y) {
    return new RefConditionGenerator(op, x, y);
  }

  @Override
  public SimpleExpr memoryCompare(SimpleExpr p1, SimpleExpr p2, SimpleExpr n) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memoryCopy(MethodGenerator mv, SimpleExpr destination, SimpleExpr source, SimpleExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memorySet(MethodGenerator mv, SimpleExpr pointer, SimpleExpr byteValue, SimpleExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr toVoidPointer(SimpleExpr ptrExpr) {
    return ptrExpr;
  }

  @Override
  public SimpleExpr unmarshallVoidPtrReturnValue(MethodGenerator mv, SimpleExpr voidPointer) {
    return Expressions.cast(voidPointer, METHOD_HANDLE_TYPE);
  }

  @Override
  public SimpleExpr malloc(MethodGenerator mv, SimpleExpr sizeInBytes) {
    throw new UnsupportedOperationException("Cannot malloc function pointers");
  }

  @Override
  public SimpleExpr realloc(SimpleExpr pointer, SimpleExpr newSizeInBytes) {
    throw new InternalCompilerException("Cannot realloc function pointers");
  }

  @Override
  public SimpleExpr pointerPlus(SimpleExpr pointer, SimpleExpr offsetInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr valueOf(SimpleExpr pointerExpr) {
    return pointerExpr;
  }
}
