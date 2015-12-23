package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.runtime.ObjectPtr;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for a record pointer parameter that may point to more than one record value, implemented using
 * a {@link ObjectPtr} reference, where each element of the {@link ObjectPtr#array} is an instance of the 
 * JVM class backing the parameter's record type.
 */
public class RecordFatPtrParamStrategy extends ParamStrategy {
  
  private RecordClassGenerator recordClassGenerator;

  public RecordFatPtrParamStrategy(RecordClassGenerator recordClassGenerator) {
    this.recordClassGenerator = recordClassGenerator;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(ObjectPtr.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    parameterValueGenerator.emitPushPointerWrapper(mv);
  }

}
