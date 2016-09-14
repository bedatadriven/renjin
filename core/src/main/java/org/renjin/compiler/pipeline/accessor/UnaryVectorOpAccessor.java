package org.renjin.compiler.pipeline.accessor;

import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Type;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.sexp.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.renjin.repackaged.asm.Opcodes.INVOKESTATIC;

public class UnaryVectorOpAccessor extends Accessor {


  private int operandIndex;
  private Accessor operandAccessor;
  private final Class<?> operandType;
  private Method applyMethod;
  private Class<?> returnType;

  public UnaryVectorOpAccessor(DeferredNode node, InputGraph inputGraph) {
    this.operandIndex = inputGraph.getOperandIndex(node);
    this.operandAccessor = Accessors.create(node.getOperands().get(0), inputGraph);

    applyMethod = findStaticApply(node.getVector());
    assert applyMethod != null;

    this.operandType = applyMethod.getParameterTypes()[0];
    returnType = applyMethod.getReturnType();
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
        
        if(supportedType(method.getReturnType()) &&
            supportedType(method.getParameterTypes()[0])) {
          return method;
        }
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
  public boolean mustCheckForIntegerNAs() {
    return operandAccessor.mustCheckForIntegerNAs();
  }

  private void pushResult(ComputeMethod method, Optional<Label> integerNaLabel) {
    if (operandType.equals(double.class)) {
      operandAccessor.pushElementAsDouble(method, integerNaLabel);

    } else if(operandType.equals(int.class)) {
      operandAccessor.pushElementAsInt(method, integerNaLabel);

    } else {
      throw new UnsupportedOperationException("operandType: " + operandType);
    }

    MethodVisitor mv = method.getVisitor();
    mv.visitMethodInsn(INVOKESTATIC,
        Type.getInternalName(applyMethod.getDeclaringClass()),
        applyMethod.getName(),
        Type.getMethodDescriptor(applyMethod), false);
  }
  
  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    pushResult(method, integerNaLabel);
    cast(method.getVisitor(), returnType, double.class);
  }


  @Override
  public void pushElementAsInt(ComputeMethod method, Optional<Label> naLabel) {
    pushResult(method, naLabel);
    cast(method.getVisitor(), returnType, int.class);
  }
}
