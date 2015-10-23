package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.*;

/**
 * Variable 
 */
public class ComplexArrayPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {

  private final GimpleIndirectType pointerType;
  private final GimpleArrayType arrayType;
  private final GimpleComplexType complexType;
  private final Type partType;
  
  private int arrayIndex;
  private int offsetIndex;

  public ComplexArrayPtrVarGenerator(GimpleIndirectType pointerType, int arrayIndex, int offsetIndex) {
    this.pointerType = pointerType;
    this.arrayType = pointerType.getBaseType();
    this.complexType = (GimpleComplexType) arrayType.getComponentType();
    this.partType = complexType.getJvmPartType();
    this.arrayIndex = arrayIndex;
    this.offsetIndex = offsetIndex;
  }

  @Override
  public GimpleType getGimpleType() {
    return pointerType;
  }


  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    
  }

  @Override
  public ExprGenerator valueOf() {
    return new ArrayValue();
  }
  
  private class ArrayValue extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return arrayType;
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new Element(indexGenerator);
    }
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
    public ExprGenerator realPart() {
      return new ElementPart(indexGenerator, 0);
    }

    @Override
    public ExprGenerator imaginaryPart() {
      return new ElementPart(indexGenerator, 1);
    }
  }
  
  private class ElementPart extends AbstractExprGenerator {
    private final ExprGenerator indexGenerator;
    private final int part;

    public ElementPart(ExprGenerator indexGenerator, int part) {
      this.indexGenerator = indexGenerator;
      this.part = part;
    }


    @Override
    public GimpleType getGimpleType() {
      return new GimpleRealType(64);
    }

    @Override
    public void emitPushValue(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      emitPushIndex(mv);
      mv.visitInsn(partType.getOpcode(Opcodes.IALOAD));
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      emitPushIndex(mv);
      
      valueGenerator.emitPushValue(mv);
      
      mv.visitInsn(partType.getOpcode(Opcodes.IASTORE));
    }

    private void emitPushIndex(MethodVisitor mv) {
      // first we need the base offset within the array
      mv.visitVarInsn(Opcodes.ILOAD, offsetIndex);
      
      // Now compute the index into the array, relative to the
      // base offset. Since each element requires two double elements,
      // we have to multiply by 2 in order to get the index
      indexGenerator.emitPushValue(mv);
      mv.visitInsn(Opcodes.ICONST_2);
      mv.visitInsn(Opcodes.IMUL);
      
      // Now add the basee offset and the index offset to get the
      // absolute offset into the array
      mv.visitInsn(Opcodes.IADD);
      
      // If we want the imaginary part, than we need to add one more
      // to the index.
      if(part == 1) {
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IADD);
      }
    }
  }
}
