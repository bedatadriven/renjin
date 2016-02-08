package org.renjin.gcc.codegen.type.record.unit;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.AddressableValue;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordConstructor;
import org.renjin.gcc.codegen.type.record.RecordValueFunction;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;


public class RecordUnitPtrStrategy extends TypeStrategy<Value> {
  
  private RecordClassTypeStrategy strategy;

  public RecordUnitPtrStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RecordUnitPtrParamStrategy(strategy);
  }

  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    return new ValueFieldStrategy(strategy.getJvmType(), fieldName);
  }

  @Override
  public TypeStrategy pointerTo() {
    return new FatPtrStrategy(new RecordValueFunction(strategy));
  }

  @Override
  public TypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, new RecordValueFunction(strategy));
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new ValueReturnStrategy(strategy.getJvmType());
  }

  @Override
  public Value varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {

      // Declare this as a Unit array so that we can get a FatPtrExpr if needed
      Value unitArray = allocator.reserveUnitArray(decl.getName(), strategy.getJvmType(), 
          Optional.<Value>of(new RecordConstructor(strategy)));

      FatPtrExpr address = new FatPtrExpr(unitArray);
      Value instance = Values.elementAt(unitArray, 0);
      
      return new AddressableValue(instance, address);
      
    } else {
      return allocator.reserve(decl.getName(), strategy.getJvmType());
    }
  }

  @Override
  public Value malloc(MethodGenerator mv, Value length) {

    if (isUnitConstant(length)) {
      throw new InternalCompilerException(getClass().getSimpleName() + " does not support (T)malloc(size) where " +
          "size != sizeof(T). This is probably because of a mistake in the choice of strategy by the compiler.");
    }
    return new RecordConstructor(strategy);
  }

  @Override
  public Value nullPointer() {
    return Values.nullRef(strategy.getJvmType());
  }

  @Override
  public ExprGenerator valueOf(Value pointerExpr) {
    return pointerExpr;
  }

  private boolean isUnitConstant(Value length) {
    if(!(length instanceof ConstantValue)) {
      return false;
    }
    ConstantValue constantValue = (ConstantValue) length;
    return constantValue.getType().equals(Type.INT_TYPE) && constantValue.getIntValue() == 1;
  }
}
