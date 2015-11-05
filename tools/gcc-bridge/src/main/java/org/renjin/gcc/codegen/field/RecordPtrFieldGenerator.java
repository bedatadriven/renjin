package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class RecordPtrFieldGenerator extends FieldGenerator {
  private String className;
  private String fieldName;
  private RecordClassGenerator recordGenerator;

  public RecordPtrFieldGenerator(String className, String fieldName, RecordClassGenerator recordGenerator) {
    this.className = className;
    this.fieldName = fieldName;
    this.recordGenerator = recordGenerator;
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    assertNoInitialValue(decl);

    emitField(ACC_PUBLIC | ACC_STATIC, cv);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, recordGenerator.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    return new StaticFieldExpr();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new Member(instanceGenerator);
  }
  
  private class StaticFieldExpr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(recordGenerator.getGimpleType());
    }
  }
  
  private class Member extends AbstractExprGenerator {

    private ExprGenerator instanceGenerator;

    public Member(ExprGenerator instanceGenerator) {
      this.instanceGenerator = instanceGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(recordGenerator.getGimpleType());
    }
  }

}
