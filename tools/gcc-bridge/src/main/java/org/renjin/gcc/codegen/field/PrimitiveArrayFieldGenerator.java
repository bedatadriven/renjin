package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;


public class PrimitiveArrayFieldGenerator extends FieldGenerator {
  private String className;
  private String fieldName;
  private GimpleArrayType arrayType;
  private GimplePrimitiveType componentType;
  private final String fieldDescriptor;
  
  public PrimitiveArrayFieldGenerator(String className, String fieldName, GimpleArrayType arrayType) {
    this.className = className;
    this.fieldName = fieldName;
    this.arrayType = arrayType;
    this.componentType = (GimplePrimitiveType) arrayType.getComponentType();
    this.fieldDescriptor = "[" + componentType.jvmType().getDescriptor();
  }

  @Override
  public GimpleType getType() {
    return arrayType;
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    cv.visitField(ACC_STATIC | ACC_PUBLIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    throw new UnimplementedException(getClass(), "staticExprGenerator");
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberExpr();
  }
  
  private class MemberExpr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return arrayType;
    }
  }
}
