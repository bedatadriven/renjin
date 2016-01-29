package org.renjin.gcc.codegen.type.record.fat;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
import org.renjin.gcc.codegen.var.Var;
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
  public void emitPushPtrArrayAndOffset(MethodGenerator mv) {

    // Allocate the new array
    sizeGenerator.emitPrimitiveValue(mv);
    PrimitiveConstGenerator.emitInt(mv, strategy.getGimpleType().sizeOf());
    mv.visitInsn(Opcodes.IDIV);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, strategy.getJvmType().getInternalName());


    int constantCount = isConstantCount(sizeGenerator);

    if(constantCount < 0 || constantCount > MAX_LOOP_UNROLLS) {
      forEachElement(mv);
    } else {
      unrollConstructorCalls(mv, constantCount);
    }
    mv.visitInsn(Opcodes.ICONST_0);
  }
  
  private void forEachElement(MethodGenerator mv) {
    // MOST unfortunately, we need to construct and initialize EACH element of the array
    // individually using a for loop
    
    // Need some temporary variables
    Var array = mv.getLocalVarAllocator().reserveArrayRef("___tmp", strategy.getJvmType());
    Var index = mv.getLocalVarAllocator().reserveInt("___tmpi");
    
    // Currently on stack:
    // Record[]
    array.store(mv);
    
    // Initialize index
    mv.iconst(0);
    index.store(mv);
    
    // Labels:
    Label loopHead = new Label();
    Label loopExit = new Label();
    
    // check: i >= array.length
    index.load(mv);
    array.load(mv);
    mv.arraylength();
    mv.ificmpge(loopExit);
    
    // initialize element i
    array.load(mv);
    index.load(mv);
    mv.anew(strategy.getJvmType());
    mv.dup();
    mv.invokespecial(strategy.getJvmType().getInternalName(), "<init>", "()V", false);
    mv.astore(strategy.getJvmType());
    
    mv.goTo(loopHead);
    
    mv.mark(loopExit);
    
    // Done! load the array back onto the stack
    array.load(mv);
  }

  private void unrollConstructorCalls(MethodGenerator mv, int constantCount) {
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


  private int isConstantCount(ExprGenerator sizeGenerator) {
    if(sizeGenerator instanceof PrimitiveConstGenerator) {
      return ((PrimitiveConstGenerator) sizeGenerator).getValue().intValue() /
          strategy.getGimpleType().sizeOf();
    } else {
      return -1;
    }
  }
}
