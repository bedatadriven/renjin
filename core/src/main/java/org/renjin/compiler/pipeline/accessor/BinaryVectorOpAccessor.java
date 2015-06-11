package org.renjin.compiler.pipeline.accessor;

import com.google.common.base.Optional;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.sexp.Vector;
import soot.JastAddJ.Opt;

import javax.swing.text.html.Option;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public class BinaryVectorOpAccessor extends Accessor {

  private Accessor operandAccessor1;
  private Accessor operandAccessor2;
  private int lengthLocal1;
  private int lengthLocal2;
  private int lengthLocal;
  private Method applyMethod;
  private Class argumentType;

  public BinaryVectorOpAccessor(DeferredNode node, InputGraph inputGraph) {
    this.operandAccessor1 = Accessors.create(node.getOperands().get(0), inputGraph);
    this.operandAccessor2 = Accessors.create(node.getOperands().get(1), inputGraph);
    applyMethod = findStaticApply(node.getVector());
    assert applyMethod != null;
    argumentType = applyMethod.getParameterTypes()[0];
  }

  public static boolean accept(DeferredNode node) {
    return findStaticApply(node.getVector()) != null;
  }

  private static Method findStaticApply(Vector vector) {
    for (Method method : vector.getClass().getMethods()) {
      if (method.getName().equals("compute") &&
              Modifier.isPublic(method.getModifiers()) &&
              Modifier.isStatic(method.getModifiers())) {

        if (Arrays.equals(method.getParameterTypes(), new Class[]{double.class, double.class}) ||
                Arrays.equals(method.getParameterTypes(), new Class[]{int.class, int.class})) {

          return method;
        }
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
  public void pushElementAsInt(ComputeMethod method, Optional<Label> integerNaLabel) {
    if (argumentType.equals(double.class)) {
      computeDouble(method, integerNaLabel);
      method.getVisitor().visitInsn(D2I);
    } else if(argumentType.equals(int.class)) {
      computeInt(method, integerNaLabel);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return operandAccessor1.mustCheckForIntegerNAs() || operandAccessor2.mustCheckForIntegerNAs();
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> naIntegerLabel) {
    if (argumentType.equals(double.class)) {
      computeDouble(method, naIntegerLabel);
    } else if (argumentType.equals(int.class)) {
      computeInt(method, naIntegerLabel);
      method.getVisitor().visitInsn(I2D);
    } else {
      throw new UnsupportedOperationException(argumentType.getName());
    }
  }


  private void computeInt(ComputeMethod method, Optional<Label> naLabel) {

    // If we've been asked to handle NA checking, then we have to set up our
    // internal NA handler block to handle the case that one of the arguments
    // is NA so that we can clean up the stack before jumping to the outer naLabel.

    // The Java bytecode verifier will not accept that multiple execution paths
    // arrive at the same point with different types on the stack.

    Optional<Label> argNaLabel = Optional.absent();
    if(naLabel.isPresent() &&
        (operandAccessor1.mustCheckForIntegerNAs() || operandAccessor2.mustCheckForIntegerNAs())) {
      argNaLabel = Optional.of(new Label());
    }

    Optional<Label> done = Optional.absent();
    if(argNaLabel.isPresent()) {
      done = Optional.of(new Label());
    }

    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP);
    mv.visitVarInsn(ILOAD, lengthLocal1);
    // stack => { index, index, length1 }
    mv.visitInsn(IREM);
    // stack => { index, index1 }
    
    operandAccessor1.pushElementAsInt(method, argNaLabel);

    // stack => { index, value1 }
    mv.visitInsn(SWAP); 
    // stack => { value1, index}
    mv.visitVarInsn(ILOAD, lengthLocal2);
    // stack => { value1, index, length2 }
    mv.visitInsn(IREM);
    // stack => { value1, index2 }

    operandAccessor2.pushElementAsInt(method, argNaLabel);
    // stack => { value1, value2}

    mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(applyMethod.getDeclaringClass()),
            applyMethod.getName(),
            Type.getMethodDescriptor(applyMethod));

    if(done.isPresent()) {
      mv.visitJumpInsn(GOTO, done.get());
    }

    if(argNaLabel.isPresent()) {
      mv.visitLabel(argNaLabel.get());
      // upon arriving here, the stack either contains
      // { index, value1 } if is.na(arg1), or
      // { value1, value2 } if !is.na(arg1) && is.na(arg2).
      // in either case, we have to get rid of one of the ints
      // so that we jump to the outer na block, there is exactly
      // one extra int on the stack as expected.
      mv.visitInsn(POP);
      mv.visitJumpInsn(GOTO, naLabel.get());
    }

    if(done.isPresent()) {
      mv.visitLabel(done.get());
    }
  }

  private void computeDouble(ComputeMethod method, Optional<Label> naIntegerLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP);
    mv.visitVarInsn(ILOAD, lengthLocal1);
    // stack => { index, index, length1 }
    mv.visitInsn(IREM);
    // stack => { index, index1 }
    operandAccessor1.pushElementAsDouble(method, naIntegerLabel);
    // stack => { index, [value1, value1] }
    mv.visitInsn(DUP2_X1); // next two instructions equivalent to swap
    mv.visitInsn(POP2);
    // stack => { value1, index}
    mv.visitVarInsn(ILOAD, lengthLocal2);
    // stack => { value1, index, length2 }
    mv.visitInsn(IREM);
    // stack => { value1, index2 }
    operandAccessor2.pushElementAsDouble(method, naIntegerLabel);
    // stack => { value1, value2}

    mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(applyMethod.getDeclaringClass()),
            applyMethod.getName(),
            Type.getMethodDescriptor(applyMethod));
  }
}
