package org.renjin.gcc.codegen.type.record.unit;

import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordValue;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;


public class RecordUnitPtrStrategy extends TypeStrategy {
  
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
  public ReturnStrategy getReturnStrategy() {
    return new ValueReturnStrategy(strategy.getJvmType());
  }

  @Override
  public RecordValue varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
//      return new AddressableRecordUnitPtrVarGenerator(strategy,
//          allocator.reserveArrayRef(decl.getName(), strategy.getJvmType()));

      throw new UnsupportedOperationException();
      
    } else {
      return new RecordValue(strategy, allocator.reserve(decl.getName(), strategy.getJvmType()));
    }
  }

  @Override
  public ExprGenerator mallocExpression(ExprGenerator size) {
    
//    if(!size.isConstantIntEqualTo(strategy.getRecordType().sizeOf())) {
//      throw new InternalCompilerException(getClass().getSimpleName() + " does not support (T)malloc(size) where " +
//          "size != sizeof(T). This is probably because of a mistake in the choice of strategy by the compiler.");
//    }
//    
   // TODO return new RecordUnitMallocGenerator(strategy);
    throw new UnsupportedOperationException();
  }


}
