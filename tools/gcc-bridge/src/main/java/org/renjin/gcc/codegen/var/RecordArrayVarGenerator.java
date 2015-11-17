package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveConstValueGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

public class RecordArrayVarGenerator extends AbstractExprGenerator implements VarGenerator {

  private final int varIndex;
  private GimpleArrayType arrayType;
  private RecordClassGenerator generator;

  public RecordArrayVarGenerator(GimpleArrayType arrayType, RecordClassGenerator generator, int varIndex) {
    this.arrayType = arrayType;
    this.generator = generator;
    this.varIndex = varIndex;
    Preconditions.checkArgument(arrayType.getLbound() == 0);
  }
  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    PrimitiveConstValueGenerator.emitInt(mv, arrayType.getElementCount());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, generator.getType().getInternalName());
    for(int i=0;i<arrayType.getElementCount();++i) {
      // keep the array on the stack
      mv.visitInsn(Opcodes.DUP);

      // index of the element to store
      PrimitiveConstValueGenerator.emitInt(mv, i);
      
      // create a new instance of the record class
      generator.emitConstructor(mv);
      
      // store the new instance to the array
      mv.visitInsn(Opcodes.AASTORE);
    }
    
    // Store the array to the local variable
    mv.visitVarInsn(Opcodes.ASTORE, varIndex);
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new Element(indexGenerator);  
  }

  @Override
  public ExprGenerator addressOf() {
    return new Pointer();
  }

  private class Element extends AbstractExprGenerator {
    private ExprGenerator indexGenerator;

    public Element(ExprGenerator indexGenerator) {
      this.indexGenerator = indexGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return arrayType.getComponentType();
    }

    @Override
    public void emitPushRecordRef(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, varIndex);
      indexGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(Opcodes.AALOAD);
    }

    @Override
    public ExprGenerator memberOf(String memberName) {
      return generator.getFieldGenerator(memberName).memberExprGenerator(this);
    }
  }
  
  private class Pointer extends AbstractExprGenerator {


    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(arrayType);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, varIndex);
      mv.visitInsn(Opcodes.ICONST_0);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.OBJECT_PTR;
    }
  }
}
