package org.renjin.gcc.codegen.type.record.fat;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.type.record.RecordArrayConstructor;
import org.renjin.gcc.codegen.type.record.RecordArrayVarGenerator;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Strategy for record types that are allocated in blocks with more than one record. 
 */
public class RecordFatPtrStrategy extends TypeStrategy {

  private final RecordTypeStrategy strategy;

  public RecordFatPtrStrategy(RecordTypeStrategy strategy) {
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
  
  

}
