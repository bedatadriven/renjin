package org.renjin.gcc.codegen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.repackaged.guava.base.Preconditions;


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

  public void invokeinterface(Class<?> declaringClass, String methodName, Type returnType, Type... argumentTypes) {
    invokeinterface(Type.getType(declaringClass).getInternalName(),
        methodName,
        Type.getMethodDescriptor(returnType, argumentTypes));
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

  /**
   * Writes an invocation of {@link System#arraycopy(Object, int, Object, int, int)}.
   * 
   * @param      src      the source array.
   * @param      srcPos   starting position in the source array.
   * @param      dest     the destination array.
   * @param      destPos  starting position in the destination data.
   * @param      length   the number of array elements to be copied.
   */
  public void arrayCopy(SimpleExpr src, SimpleExpr srcPos, SimpleExpr dest, SimpleExpr destPos, SimpleExpr length) {

    Preconditions.checkArgument(srcPos.getType().equals(Type.INT_TYPE), "srcPos must have type int");
    Preconditions.checkArgument(destPos.getType().equals(Type.INT_TYPE), "destPos must have type int");
    Preconditions.checkArgument(length.getType().equals(Type.INT_TYPE), "length must have type int");
    
    src.load(this);
    srcPos.load(this);
    dest.load(this);
    destPos.load(this);
    length.load(this);

    invokestatic(System.class, "arraycopy", Type.getMethodDescriptor(Type.VOID_TYPE, 
        Type.getType(Object.class), Type.INT_TYPE,
        Type.getType(Object.class), Type.INT_TYPE,
        Type.INT_TYPE));
    
  }

}
