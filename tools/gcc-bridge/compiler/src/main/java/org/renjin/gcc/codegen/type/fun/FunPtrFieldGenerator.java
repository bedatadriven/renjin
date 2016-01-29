package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.invoke.MethodHandle;

/**
 * Field containing a pointer to a function 
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
    return new GimplePointerType(functionType);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(Opcodes.ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, Type.getDescriptor(MethodHandle.class), null, null).visitEnd();
  }
  
  public void emitStoreMember(MethodGenerator mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushMethodHandle(mv);
    mv.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, Type.getDescriptor(MethodHandle.class));
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
    public void emitPushMethodHandle(MethodGenerator mv) {
      instanceGenerator.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, fieldName, Type.getDescriptor(MethodHandle.class));
    }

    @Override
    public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
      instanceGenerator.emitPushRecordRef(mv);
      emitStoreMember(mv, valueGenerator);
    }

    @Override
    public void emitPushPtrRefForNullComparison(MethodGenerator mv) {
      emitPushMethodHandle(mv);
    }
  }

}
