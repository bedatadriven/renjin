package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.runtime.ObjectPtr;


public class RecordUnitPtrStrategy extends TypeStrategy {
  
  private RecordTypeStrategy strategy;

  public RecordUnitPtrStrategy(RecordTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RecordUnitPtrParamStrategy(strategy);
  }

  @Override
  public FieldGenerator fieldGenerator(String className, String fieldName) {
    return new RecordPtrFieldGenerator(className, fieldName, strategy);
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new RecordUnitPtrReturnStrategy(strategy);
  }

  @Override
  public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      return new AddressableRecordUnitPtrVarGenerator(strategy,
          allocator.reserveArrayRef(decl.getName(), strategy.getJvmType()));

    } else {
      return new RecordUnitPtrVarGenerator(strategy, allocator.reserve(decl.getName(), strategy.getJvmType()));
    }
  }

  @Override
  public TypeStrategy pointerTo() {
    return new PointerPointer();
  }

  @Override
  public ExprGenerator mallocExpression(ExprGenerator size) {
    
    if(!size.isConstantIntEqualTo(strategy.getRecordType().sizeOf())) {
      throw new InternalCompilerException(getClass().getSimpleName() + " does not support (T)malloc(size) where " +
          "size != sizeof(T). This is probably because of a mistake in the choice of strategy by the compiler.");
    }
    
    return new RecordUnitMallocGenerator(strategy);
  }

  public class PointerPointer extends TypeStrategy {

    @Override
    public ParamStrategy getParamStrategy() {
      return new RecordUnitPtrPtrParamStrategy(strategy);
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return new RecordPtrPtrVarGenerator(strategy,
          allocator.reserve(decl.getName(), ObjectPtr.class),
          allocator.reserveInt(decl.getName() + "$offset"));
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      return new MallocGenerator(
          strategy.getRecordType().pointerTo().pointerTo(),
          Type.getType(ObjectPtr.class),
          GimplePointerType.SIZE_OF,
          size);
    }

    @Override
    public ReturnStrategy getReturnStrategy() {
      return new RecordUnitPtrPtrReturnStrategy();
    }
  }

}
