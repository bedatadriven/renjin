package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleType;


public class PrimitiveArrayFieldGenerator extends FieldGenerator {
  private String className;
  private String fieldName;

  public PrimitiveArrayFieldGenerator(String className, String fieldName) {
    this.className = className;
    this.fieldName = fieldName;
  }

  @Override
  public GimpleType getType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {

  }

  @Override
  public ExprGenerator staticExprGenerator() {
    return null;
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return null;
  }
}
