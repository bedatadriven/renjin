package org.renjin.gcc.codegen.type.fun;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.gimple.type.GimpleFunctionType;

import java.lang.invoke.MethodHandle;

/**
 * A field pointing to one or more function pointers. Compiled as an array+offset of MethodHandles.
 */
public class FunPtrPtrField extends FieldStrategy {

  private String className;
  private String arrayFieldName;
  private final String arrayFieldDescriptor;
  private final String offsetFieldName;
  private GimpleFunctionType functionType;

  public FunPtrPtrField(String className, String fieldName, GimpleFunctionType functionType) {
    this.className = className;
    this.arrayFieldName = fieldName;
    this.arrayFieldDescriptor = "[" + Type.getDescriptor(MethodHandle.class);
    this.offsetFieldName = fieldName + "$offset";
    this.functionType = functionType;
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayFieldName, arrayFieldDescriptor, null, null).visitEnd();
    cv.visitField(Opcodes.ACC_PUBLIC, offsetFieldName, "I", null, 0).visitEnd();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberExpr(instanceGenerator);
  }
  
  private class MemberExpr extends AbstractExprGenerator {
    private ExprGenerator instance;

    public MemberExpr(ExprGenerator instance) {
      this.instance = instance;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
      instance.emitPushRecordRef(mv);
      mv.visitInsn(Opcodes.DUP);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, arrayFieldName, arrayFieldDescriptor);
      // stack : [instance, array]
      mv.visitInsn(Opcodes.SWAP);
      // stack: [array, instance]
      mv.visitFieldInsn(Opcodes.GETFIELD, className, offsetFieldName, "I");
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedFunPtr(this);
    }
  }
  
}
