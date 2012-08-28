package org.renjin.compiler.pipeline.accessor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.sexp.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.*;

public class BinaryVectorOpAccessor extends Accessor {

  private Accessor operandAccessor1;
  private Accessor operandAccessor2;
  private int lengthLocal1;
  private int lengthLocal2;
  private int lengthLocal;
  private Method applyMethod;

  public BinaryVectorOpAccessor(DeferredNode node, InputGraph inputGraph) {
    this.operandAccessor1 = Accessors.create(node.getOperands().get(0), inputGraph);
    this.operandAccessor2 = Accessors.create(node.getOperands().get(1), inputGraph);
    applyMethod = findStaticApply(node.getVector());
    assert applyMethod != null;
  }

  public static boolean accept(DeferredNode node) {
    return findStaticApply(node.getVector()) != null;
  }

  private static Method findStaticApply(Vector vector) {
    for(Method method : vector.getClass().getMethods()) {
      if(method.getName().equals("compute") &&
              Modifier.isPublic(method.getModifiers()) &&
              Modifier.isStatic(method.getModifiers()) &&
              method.getParameterTypes().length == 2) {
        return method;
      }
    }
    return null;
  }

  @Override
  public void init(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    operandAccessor1.init(method);
    operandAccessor2.init(method);
    lengthLocal1 = method.reserveLocal(1);
    lengthLocal2 = method.reserveLocal(1);
    lengthLocal = method.reserveLocal(1);
    operandAccessor1.pushLength(method);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, lengthLocal1);
    operandAccessor2.pushLength(method);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, lengthLocal2);
    method.getVisitor().visitMethodInsn(INVOKESTATIC, "java/lang/Math", "max", "(II)I");
    mv.visitVarInsn(ISTORE, lengthLocal);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    method.getVisitor().visitVarInsn(ILOAD, lengthLocal);
  }

  @Override
  public void pushDouble(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP);
    mv.visitVarInsn(ILOAD, lengthLocal1);
    // stack => { index, index, length1 }
    mv.visitInsn(IREM);
    // stack => { index, index1 }
    operandAccessor1.pushDouble(method);
    // stack => { index, [value1, value1] }
    mv.visitInsn(DUP2_X1); // next two instructions equivalent to swap
    mv.visitInsn(POP2);
    // stack => { value1, index}
    mv.visitVarInsn(ILOAD, lengthLocal2);
    // stack => { value1, index, length2 }
    mv.visitInsn(IREM);
    // stack => { value1, index2 }
    operandAccessor2.pushDouble(method);
    // stack => { value1, value2}

    mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(applyMethod.getDeclaringClass()),
            applyMethod.getName(),
            Type.getMethodDescriptor(applyMethod));

 }
}
