package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.runtime.ObjectPtr;

import java.util.Collections;
import java.util.List;


public class WrappedRecordPtrParamGenerator extends ParamGenerator {
  
  private RecordClassGenerator recordClassGenerator;

  public WrappedRecordPtrParamGenerator(RecordClassGenerator recordClassGenerator) {
    this.recordClassGenerator = recordClassGenerator;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(ObjectPtr.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, int startIndex, LocalVarAllocator localVars) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    parameterValueGenerator.emitPushPointerWrapper(mv);
  }
}
