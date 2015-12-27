package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;


public class RecordArrayConstructor extends AbstractExprGenerator {

  private RecordClassGenerator generator;
  private final GimpleArrayType arrayType;
  private List<ExprGenerator> elements;

  public RecordArrayConstructor(RecordClassGenerator generator, GimpleArrayType arrayType, List<ExprGenerator> elements) {
    this.generator = generator;
    this.arrayType = arrayType;
    this.elements = elements;
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    // create a new array
    PrimitiveConstGenerator.emitInt(mv, arrayType.getElementCount());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, generator.getClassName());
    
    // initialize the elements
    for (int i = 0; i < arrayType.getElementCount(); i++) {
      // keep the array on the stack
      mv.visitInsn(Opcodes.DUP);
      
      // push the index onto the stack
      PrimitiveConstGenerator.emitInt(mv, i);
      
      // push the record instance onto the stack
      elements.get(i).emitPushRecordRef(mv);
      
      // store to the array
      mv.visitInsn(Opcodes.AASTORE);
    }
  }
}
