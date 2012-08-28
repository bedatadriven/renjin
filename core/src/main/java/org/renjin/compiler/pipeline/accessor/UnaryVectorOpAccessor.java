package org.renjin.compiler.pipeline.accessor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.sexp.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class UnaryVectorOpAccessor extends Accessor {


  private int operandIndex;
  private Accessor operandAccessor;
  private Method applyMethod;

  public UnaryVectorOpAccessor(DeferredNode node, InputGraph inputGraph) {
    this.operandIndex = inputGraph.getOperandIndex(node);
    this.operandAccessor = Accessors.create(node.getOperands().get(0), inputGraph);

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
              method.getParameterTypes().length == 1) {
        return method;
      }
    }
    return null;
  }

  @Override
  public void init(ComputeMethod method) {
    operandAccessor.init(method);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    operandAccessor.pushLength(method);
  }

  @Override
  public void pushDouble(ComputeMethod method) {
    operandAccessor.pushDouble(method);
    MethodVisitor mv = method.getVisitor();
    mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(applyMethod.getDeclaringClass()),
            applyMethod.getName(),
            Type.getMethodDescriptor(applyMethod));

 }
}
