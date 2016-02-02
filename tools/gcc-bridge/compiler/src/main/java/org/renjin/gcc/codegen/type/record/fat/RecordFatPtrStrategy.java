package org.renjin.gcc.codegen.type.record.fat;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Strategy for record types that are allocated in blocks with more than one record. 
 */
public class RecordFatPtrStrategy extends TypeStrategy {

  private final RecordClassTypeStrategy strategy;

  public RecordFatPtrStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }
  

  public Type getJvmArrayType() {
    return Type.getType("[" + strategy.getJvmType().getDescriptor());
  }

  public GimpleType getGimpleType() {
    return strategy.getRecordType().pointerTo();
  }

  public Type getJvmType() {
    return strategy.getJvmType();
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RecordFatPtrParamStrategy(RecordFatPtrStrategy.this);
  }


  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    return new RecordFatPtrFieldStrategy(className, fieldName, strategy);
  }

  @Override
  public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
    Var array = allocator.reserve(decl.getName(), getJvmArrayType());
    Var offset = allocator.reserveInt(decl.getName() + "$offset");
    
    return new RecordFatPtrVarGenerator(strategy, array, offset);
  }

  @Override
  public ExprGenerator mallocExpression(ExprGenerator size) {
    return new RecordFatPtrMallocGenerator(this, size);
  }
}
