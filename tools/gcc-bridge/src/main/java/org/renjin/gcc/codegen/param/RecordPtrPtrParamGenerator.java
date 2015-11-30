package org.renjin.gcc.codegen.param;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.DereferencedUnitRecordPtr;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;

import java.util.Collections;
import java.util.List;

public class RecordPtrPtrParamGenerator extends ParamGenerator {
  
  private RecordClassGenerator generator;

  public RecordPtrPtrParamGenerator(RecordClassGenerator generator) {
    this.generator = generator;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(ObjectPtr.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, int startIndex, LocalVarAllocator localVars) {
    return new ParamExpr(startIndex);
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    parameterValueGenerator.emitPushPointerWrapper(mv);
  }

  @Override
  public GimpleType getGimpleType() {
    return generator.getGimpleType();
  }
  
  private class ParamExpr extends AbstractExprGenerator {

    private int varIndex;

    public ParamExpr(int varIndex) {
      this.varIndex = varIndex;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(new GimplePointerType(generator.getGimpleType()));
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedUnitRecordPtr(this);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, varIndex);
      Type arrayType = Type.getType("[" + generator.getType().getDescriptor());
      WrapperType.OBJECT_PTR.emitUnpackArrayAndOffset(mv, Optional.of(arrayType));
    }

    @Override
    public void emitPushPtrRefForNullComparison(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, varIndex);
      mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(ObjectPtr.class), "array", "[Ljava/lang/Object;");
    }
  }
}
