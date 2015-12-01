package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.DereferencedRecordPtr;
import org.renjin.gcc.codegen.pointers.RecordPtrPtrPlus;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Pointer to pointers of Records.
 * 
 * <p>Compile using an array of ObjectPtr</p>
 */
public class RecordPtrPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private RecordClassGenerator recordClassGenerator;
  private final int arrayIndex;
  private final int offsetIndex;

  public RecordPtrPtrVarGenerator(RecordClassGenerator recordClassGenerator, int arrayIndex, int offsetIndex) {
    this.recordClassGenerator = recordClassGenerator;
    this.arrayIndex = arrayIndex;
    this.offsetIndex = offsetIndex;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
    mv.visitVarInsn(Opcodes.ILOAD, offsetIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushPtrArrayAndOffset(mv);
    mv.visitVarInsn(Opcodes.ISTORE, offsetIndex);
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(new GimplePointerType(recordClassGenerator.getGimpleType()));
  }

  @Override
  public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
    return new RecordPtrPtrPlus(recordClassGenerator, this, offsetInBytes);
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedRecordPtr(recordClassGenerator, this);
  }
}
