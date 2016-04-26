package org.renjin.gcc.codegen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.gcc.codegen.var.LocalVarAllocator;


public class MethodGenerator extends InstructionAdapter {
  
  private final LocalVarAllocator localVarAllocator = new LocalVarAllocator();
  
  public MethodGenerator(MethodVisitor mv) {
    super(Opcodes.ASM5, mv);
  }

  public LocalVarAllocator getLocalVarAllocator() {
    return localVarAllocator;
  }
  
  public void invokestatic(Class<?> ownerClass, String methodName, String descriptor) {
    invokestatic(Type.getInternalName(ownerClass), methodName, descriptor, false);
  }
  
  public void invokestatic(Type ownerClass, String methodName, String descriptor) {
    invokestatic(ownerClass.getInternalName(), methodName, descriptor);
  }

  public void invokeconstructor(Type ownerClass, Type... argumentTypes) {
    invokespecial(ownerClass.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, argumentTypes), false);
  }
  
  public void invokevirtual(Class<?> declaringClass, String methodName, Type returnType, Type... argumentTypes) {
    invokevirtual(Type.getType(declaringClass).getInternalName(),
        methodName,
        Type.getMethodDescriptor(returnType, argumentTypes),
        declaringClass.isInterface());
  }
  
  public void invokeIdentityHashCode() {
    invokestatic(System.class, "identityHashCode", "(Ljava/lang/Object;)I");
  }
  
  public void putfield(Type declaringClass, String name, Type fieldType) {
    putfield(declaringClass.getInternalName(), name, fieldType.getDescriptor());
  }

  public void pop(Type type) {
    switch (type.getSort()) {
      case Type.VOID:
        // NOOP
        break;
    
      case Type.LONG:
      case Type.DOUBLE:
        pop2();
        break;
      
      default:
        pop();
    }
    
  }
}
