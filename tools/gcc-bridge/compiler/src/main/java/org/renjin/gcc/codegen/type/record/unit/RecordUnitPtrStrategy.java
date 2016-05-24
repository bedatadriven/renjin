package org.renjin.gcc.codegen.type.record.unit;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordConstructor;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;


public class RecordUnitPtrStrategy implements PointerTypeStrategy<SimpleExpr> {
  
  private RecordClassTypeStrategy strategy;
  private RecordUnitPtrValueFunction valueFunction;
  
  public RecordUnitPtrStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
    this.valueFunction = new RecordUnitPtrValueFunction(strategy.getJvmType());
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new SimpleParamStrategy(strategy.getJvmType());
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new SimpleFieldStrategy(fieldName, strategy.getJvmType());
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, valueFunction);
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(valueFunction);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, valueFunction);
  }

  @Override
  public SimpleExpr cast(Expr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof FatPtrStrategy) {
      FatPtrExpr ptr = (FatPtrExpr) value;
      // TODO
      // Currently we punt until runtime by triggering a ClassCastException
      return Expressions.uncheckedCast(ptr.getArray(), strategy.getJvmType());
      
    } else if(typeStrategy instanceof RecordUnitPtrStrategy) {
      return Expressions.cast((SimpleExpr) value, strategy.getJvmType());
    }
    throw new UnsupportedCastException();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new SimpleReturnStrategy(strategy.getJvmType());
  }

  @Override
  public SimpleExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {

      // Declare this as a Unit array so that we can get a FatPtrExpr if needed
      SimpleExpr unitArray = allocator.reserveUnitArray(decl.getName(), strategy.getJvmType(), Optional.<SimpleExpr>absent());

      FatPtrExpr address = new FatPtrExpr(unitArray);
      ArrayElement instance = Expressions.elementAt(unitArray, 0);
      
      return new SimpleAddressableExpr(instance, address);
      
    } else {
      return allocator.reserve(decl.getName(), strategy.getJvmType());
    }
  }

  @Override
  public SimpleExpr malloc(MethodGenerator mv, SimpleExpr sizeInBytes) {

    if (isUnitConstant(sizeInBytes)) {
      throw new InternalCompilerException(getClass().getSimpleName() + " does not support (T)malloc(size) where " +
          "size != sizeof(T). This is probably because of a mistake in the choice of strategy by the compiler.");
    }
    return new RecordConstructor(strategy);
  }

  @Override
  public SimpleExpr realloc(SimpleExpr pointer, SimpleExpr newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr pointerPlus(SimpleExpr pointer, SimpleExpr offsetInBytes) {
    // According to our analysis conducted before-hand, there should be no pointer
    // to a sequence of records of this type with more than one record, so the result should
    // be undefined.
    return Expressions.nullRef(strategy.getJvmType());
  }

  @Override
  public SimpleExpr nullPointer() {
    return Expressions.nullRef(strategy.getJvmType());
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
    return Expressions.cast(voidPointer, getJvmType());
  }

  @Override
  public Expr valueOf(SimpleExpr pointerExpr) {
    return pointerExpr;
  }

  private boolean isUnitConstant(SimpleExpr length) {
    if(!(length instanceof ConstantValue)) {
      return false;
    }
    ConstantValue constantValue = (ConstantValue) length;
    return constantValue.getType().equals(Type.INT_TYPE) && constantValue.getIntValue() == 1;
  }

  public Type getJvmType() {
    return strategy.getJvmType();
  }
}
