package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.invoke.MethodHandle;

/**
 * Generates 
 */
public class FunPtrFieldGenerator extends FieldGenerator {

  private final String className;
  private final String fieldName;
  private GimpleFunctionType functionType;

  public FunPtrFieldGenerator(String className, String fieldName, GimpleFunctionType type) {
    this.className = className;
    this.fieldName = fieldName;
    this.functionType = type;
  }

  @Override
  public GimpleType getType() {
    return functionType;
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    assertNoInitialValue(decl);
    
    emitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, cv);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(Opcodes.ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, Type.getDescriptor(MethodHandle.class), null, null).visitEnd();
  }
  
  public void emitStoreMember(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushMethodHandle(mv);
    mv.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, Type.getDescriptor(MethodHandle.class));
  }
  
  @Override
  public ExprGenerator staticExprGenerator() {
    return new StaticExpr();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberPtr(instanceGenerator);
  }
  
  private class MemberPtr extends AbstractExprGenerator {
    private ExprGenerator instanceGenerator;

    public MemberPtr(ExprGenerator instanceGenerator) {
      this.instanceGenerator = instanceGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(functionType);
    }

    @Override
    public void emitPushMethodHandle(MethodVisitor mv) {
      instanceGenerator.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, fieldName, Type.getDescriptor(MethodHandle.class));
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      instanceGenerator.emitPushRecordRef(mv);
      emitStoreMember(mv, valueGenerator);
    }

  }

  /**
   * ExprGenerator for a static field's value
   */
  private class StaticExpr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(functionType);
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      valueGenerator.emitPushMethodHandle(mv);
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, Type.getDescriptor(MethodHandle.class));
    }

    @Override
    public void emitPushMethodHandle(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, Type.getDescriptor(MethodHandle.class));
    }
  }
}
