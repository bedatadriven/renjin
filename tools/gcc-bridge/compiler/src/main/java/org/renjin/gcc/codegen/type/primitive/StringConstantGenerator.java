package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleStringConstant;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.BytePtr;

/**
 * Emits the byte code to push string literals onto the stack
 * 
 */
public class StringConstantGenerator extends AbstractExprGenerator {

  private final GimpleStringConstant constantExpr;
  private final GimpleArrayType type;

  public StringConstantGenerator(GimpleExpr value) {
    this.constantExpr = (GimpleStringConstant) value;
    this.type = constantExpr.getType();
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new ElementAt(indexGenerator);
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    int firstChar = constantExpr.getValue().charAt(0);
    PrimitiveConstGenerator.emitInt(mv, firstChar);
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    mv.visitLdcInsn(constantExpr.getValue());

    // consume the string constant and push the array reference
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getType(BytePtr.class).getInternalName(),
        "toArray", "(Ljava/lang/String;)[B", false);
  }


  private class ElementAt extends AbstractExprGenerator {

    private ExprGenerator indexGenerator;

    public ElementAt(ExprGenerator indexGenerator) {
      this.indexGenerator = indexGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimpleIntegerType(8);
    }

    @Override
    public ExprGenerator addressOf() {
      if(indexGenerator.isConstantIntEqualTo(type.getLbound())) {
        return new AddressOf();
      } else {
        return new AddressOffset(indexGenerator);
      }
    }
  }

  private class AddressOf extends AbstractExprGenerator {


    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {

      // push the string as an array
      StringConstantGenerator.this.emitPushArray(mv);

      // push the offset
      mv.visitInsn(Opcodes.ICONST_0);
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(type);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.of(Type.BYTE_TYPE);
    }

    @Override
    public ExprGenerator valueOf() {
      return StringConstantGenerator.this;
    }
  }

  private class AddressOffset extends AbstractExprGenerator {

    private final ExprGenerator indexGenerator;

    public AddressOffset(ExprGenerator indexGenerator) {
      this.indexGenerator = indexGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(type);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.of(Type.BYTE_TYPE);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      StringConstantGenerator.this.emitPushArray(mv);
      indexGenerator.emitPrimitiveValue(mv);
    }


  }
}

