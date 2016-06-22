package org.renjin.gcc.codegen.type.record.unit;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
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
import org.renjin.gcc.codegen.type.record.RecordValue;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;


public class RecordUnitPtrStrategy implements PointerTypeStrategy<RecordUnitPtr>, SimpleTypeStrategy<RecordUnitPtr> {
  
  private RecordClassTypeStrategy strategy;
  private RecordUnitPtrValueFunction valueFunction;
  
  public RecordUnitPtrStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
    this.valueFunction = new RecordUnitPtrValueFunction(strategy.getJvmType());
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RefPtrParamStrategy<>(this);
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new SimpleFieldStrategy(fieldName, strategy.getJvmType(), RecordUnitPtr.class);
  }

  @Override
  public RecordUnitPtr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
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
    return ArrayTypeStrategies.of(arrayType, valueFunction);
  }

  @Override
  public RecordUnitPtr cast(GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof FatPtrStrategy) {
      FatPtrExpr ptr = (FatPtrExpr) value;
      // TODO
      // Currently we punt until runtime by triggering a ClassCastException
      return new RecordUnitPtr(Expressions.uncheckedCast(ptr.getArray(), strategy.getJvmType()));
      
    } else if(typeStrategy instanceof RecordUnitPtrStrategy) {
      RecordUnitPtr ptrExpr = (RecordUnitPtr) value;
      return new RecordUnitPtr(Expressions.cast(ptrExpr.unwrap(), strategy.getJvmType()));
      
    } else if(typeStrategy instanceof VoidPtrStrategy) {
      VoidPtr voidPtr = (VoidPtr) value;
      return new RecordUnitPtr(Expressions.cast(voidPtr.unwrap(), strategy.getJvmType()));
    }
    throw new UnsupportedCastException();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new RecordUnitPtrReturnStrategy(strategy.getJvmType());
  }

  @Override
  public RecordUnitPtr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {

      // Declare this as a Unit array so that we can get a FatPtrExpr if needed
      JExpr unitArray = allocator.reserveUnitArray(decl.getName(), strategy.getJvmType(), Optional.<JExpr>absent());

      FatPtrExpr address = new FatPtrExpr(unitArray);
      ArrayElement instance = Expressions.elementAt(unitArray, 0);
      
      return new RecordUnitPtr(instance, address);
      
    } else {
      return new RecordUnitPtr(allocator.reserve(decl.getNameIfPresent(), strategy.getJvmType()));
    }
  }

  @Override
  public RecordUnitPtr malloc(MethodGenerator mv, JExpr sizeInBytes) {

    if (isUnitConstant(sizeInBytes)) {
      throw new InternalCompilerException(getClass().getSimpleName() + " does not support (T)malloc(size) where " +
          "size != sizeof(T). This is probably because of a mistake in the choice of strategy by the compiler.");
    }
    return new RecordUnitPtr(new RecordConstructor(strategy));
  }

  @Override
  public RecordUnitPtr realloc(RecordUnitPtr pointer, JExpr newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RecordUnitPtr pointerPlus(RecordUnitPtr pointer, JExpr offsetInBytes) {
    // According to our analysis conducted before-hand, there should be no pointer
    // to a sequence of records of this type with more than one record, so the result should
    // be undefined.
    return nullPointer();
  }

  @Override
  public RecordUnitPtr nullPointer() {
    return new RecordUnitPtr(Expressions.nullRef(strategy.getJvmType()));
  }

  @Override
  public ConditionGenerator comparePointers(GimpleOp op, RecordUnitPtr x, RecordUnitPtr y) {
    return new RefConditionGenerator(op, x.unwrap(), y.unwrap());
  }

  @Override
  public JExpr memoryCompare(RecordUnitPtr p1, RecordUnitPtr p2, JExpr n) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memoryCopy(MethodGenerator mv, RecordUnitPtr destination, RecordUnitPtr source, JExpr length, boolean buffer) {

    Type recordType = strategy.getJvmType();

    destination.unwrap().load(mv);
    source.unwrap().load(mv);
    mv.invokevirtual(recordType, "set", Type.getMethodDescriptor(Type.VOID_TYPE, recordType), false);
  }

  @Override
  public void memorySet(MethodGenerator mv, RecordUnitPtr pointer, JExpr byteValue, JExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VoidPtr toVoidPointer(RecordUnitPtr ptrExpr) {
    return new VoidPtr(ptrExpr.unwrap());
  }

  @Override
  public RecordUnitPtr unmarshallVoidPtrReturnValue(MethodGenerator mv, JExpr voidPointer) {
    return new RecordUnitPtr(Expressions.cast(voidPointer, getJvmType()));
  }

  @Override
  public RecordValue valueOf(RecordUnitPtr pointerExpr) {
    return new RecordValue(pointerExpr.unwrap());
  }

  private boolean isUnitConstant(JExpr length) {
    if(!(length instanceof ConstantValue)) {
      return false;
    }
    ConstantValue constantValue = (ConstantValue) length;
    return constantValue.getType().equals(Type.INT_TYPE) && constantValue.getIntValue() == 1;
  }

  public Type getJvmType() {
    return strategy.getJvmType();
  }

  @Override
  public RecordUnitPtr wrap(JExpr expr) {
    return new RecordUnitPtr(expr);
  }

  @Override
  public String toString() {
    return "RecordUnitPtrStrategy[" + strategy.getRecordTypeDef().getName() + "]";
  }
}
