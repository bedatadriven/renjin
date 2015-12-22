package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.Var;
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
  private final Var arrayVar;
  private final Var offsetVar;

  public RecordPtrPtrVarGenerator(RecordClassGenerator recordClassGenerator, Var arrayVar, Var offsetVar) {
    this.recordClassGenerator = recordClassGenerator;
    this.arrayVar = arrayVar;
    this.offsetVar = offsetVar;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    arrayVar.load(mv);
    offsetVar.load(mv);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushPtrArrayAndOffset(mv);
    offsetVar.store(mv);
    arrayVar.store(mv);
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
