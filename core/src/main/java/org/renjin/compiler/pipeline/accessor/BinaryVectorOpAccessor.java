package org.renjin.compiler.pipeline.accessor;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.renjin.repackaged.asm.Opcodes.*;

public class BinaryVectorOpAccessor extends Accessor {

  private Accessor[] operandAccessors = new Accessor[2];
  private int lengthLocal1;
  private int lengthLocal2;
  private int lengthLocal;
  private Method applyMethod;

  public BinaryVectorOpAccessor(DeferredNode node, InputGraph inputGraph) {
    this.operandAccessors[0] = Accessors.create(node.getOperands().get(0), inputGraph);
    this.operandAccessors[1] = Accessors.create(node.getOperands().get(1), inputGraph);
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
        
        if (supportedType(method.getReturnType()) &&
            supportedType(method.getParameterTypes()[0]) &&
            supportedType(method.getParameterTypes()[1])) {
          return method;
        }
      }
    }
    return null;
  }

  private static boolean supportedType(Class<?> type) {
    return type.equals(double.class) ||
           type.equals(int.class);
  }

  @Override
  public void init(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    operandAccessors[0].init(method);
    operandAccessors[1].init(method);
    lengthLocal1 = method.reserveLocal(1);
    lengthLocal2 = method.reserveLocal(1);
    lengthLocal = method.reserveLocal(1);
    operandAccessors[0].pushLength(method);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, lengthLocal1);
    operandAccessors[1].pushLength(method);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, lengthLocal2);
    method.getVisitor().visitMethodInsn(INVOKESTATIC, "java/lang/Math", "max", "(II)I", false);
    mv.visitVarInsn(ISTORE, lengthLocal);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    method.getVisitor().visitVarInsn(ILOAD, lengthLocal);
  }


  private void push(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP);
    mv.visitVarInsn(ILOAD, lengthLocal1);
    // stack => { index, index, length1 }
    mv.visitInsn(IREM);
    // stack => { index, index1 }
    pushOperand(method, 0);
    // stack => { index, [value1, value1] }
    mv.visitInsn(DUP2_X1); // next two instructions equivalent to swap
    mv.visitInsn(POP2);
    // stack => { value1, index}
    mv.visitVarInsn(ILOAD, lengthLocal2);
    // stack => { value1, index, length2 }
    mv.visitInsn(IREM);
    // stack => { value1, index2 }
    pushOperand(method, 1);
    // stack => { value1, value2}

    mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(applyMethod.getDeclaringClass()),
            applyMethod.getName(),
            Type.getMethodDescriptor(applyMethod), false);
  }

  private void pushOperand(ComputeMethod method, int operandIndex) {
    Class<?> parameterType = applyMethod.getParameterTypes()[operandIndex];
    if(parameterType.equals(double.class)) {
      operandAccessors[operandIndex].pushDouble(method);
    } else if(parameterType.equals(int.class)) {
      operandAccessors[operandIndex].pushInt(method);
    } else {
      throw new UnsupportedOperationException("parameterType: " + parameterType);
    }
  }

  @Override
  public void pushDouble(ComputeMethod method) {
    push(method);
    
    if(applyMethod.getReturnType().equals(int.class)) {
      method.getVisitor().visitInsn(Opcodes.I2D);
    } else if(applyMethod.getReturnType().equals(double.class)) {
      // NOOP
    } else {
      throw new UnsupportedOperationException("returnType: " + applyMethod.getReturnType());
    }
  }

  @Override
  public void pushInt(ComputeMethod method) {
    push(method);

    if(applyMethod.getReturnType().equals(int.class)) {
      // NOOP
    } else if(applyMethod.getReturnType().equals(double.class)) {
      method.getVisitor().visitInsn(Opcodes.D2I);
    } else {
      throw new UnsupportedOperationException("returnType: " + applyMethod.getReturnType());
    }  
  }
}
