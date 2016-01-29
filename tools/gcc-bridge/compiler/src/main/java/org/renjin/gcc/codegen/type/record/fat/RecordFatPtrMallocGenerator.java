package org.renjin.gcc.codegen.type.record.fat;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates malloc() calls for Record pointers
 */
public class RecordFatPtrMallocGenerator extends AbstractExprGenerator {

  public static final int MAX_LOOP_UNROLLS = 5;
  private RecordFatPtrStrategy strategy;
  private final ExprGenerator sizeGenerator;

  public RecordFatPtrMallocGenerator(RecordFatPtrStrategy strategy, ExprGenerator sizeGenerator) {
    this.strategy = strategy;
    this.sizeGenerator = sizeGenerator;
  }

  @Override
  public GimpleType getGimpleType() {
    return strategy.getGimpleType();
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {

    // Allocate the new array
    sizeGenerator.emitPrimitiveValue(mv);
    PrimitiveConstGenerator.emitInt(mv, strategy.getGimpleType().sizeOf());
    mv.visitInsn(Opcodes.IDIV);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, strategy.getJvmType().getInternalName());


    int constantCount = isConstantCount(sizeGenerator);

    if(constantCount < 0 || constantCount > MAX_LOOP_UNROLLS) {
      
      throw new UnsupportedOperationException("TODO: dynamic record allocation size");
    } else {


      Type type = strategy.getJvmType();

      for(int i=0; i< constantCount; ++i) {
        mv.visitInsn(Opcodes.DUP);
        PrimitiveConstGenerator.emitInt(mv, i);
        mv.visitTypeInsn(Opcodes.NEW, type.getInternalName());
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, type.getInternalName(), "<init>", "()V", false);
        mv.visitInsn(Opcodes.AASTORE);
      }
    }
    mv.visitInsn(Opcodes.ICONST_0);
  }
  
  

  private int isConstantCount(ExprGenerator sizeGenerator) {
    if(sizeGenerator instanceof PrimitiveConstGenerator) {
      return ((PrimitiveConstGenerator) sizeGenerator).getValue().intValue() /
          strategy.getGimpleType().sizeOf();
    } else {
      return -1;
    }
  }
}
