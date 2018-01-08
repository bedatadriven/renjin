/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.pipeliner.fusion.node;

import org.renjin.pipeliner.ComputeMethod;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.sexp.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.renjin.repackaged.asm.Opcodes.*;

public class BinaryVectorOpNode extends LoopNode {

  private String operatorName;
  private LoopNode[] operands = new LoopNode[2];
  private int lengthLocal1;
  private int lengthLocal2;
  private int lengthLocal;
  private Method applyMethod;
  private Class argumentType;

  public BinaryVectorOpNode(String operatorName, Method operator, LoopNode x, LoopNode y) {
    this.operatorName = operatorName;
    this.operands[0] = x;
    this.operands[1] = y;
    applyMethod = operator;
    assert applyMethod != null;
    argumentType = applyMethod.getParameterTypes()[0];
  }

  public static Method findMethod(Vector vector) {
    for (Method method : vector.getClass().getMethods()) {
      if (method.getName().equals("compute") &&
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

  @Override
  public void init(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    operands[0].init(method);
    operands[1].init(method);
    lengthLocal1 = method.reserveLocal(1);
    lengthLocal2 = method.reserveLocal(1);
    lengthLocal = method.reserveLocal(1);
    operands[0].pushLength(method);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, lengthLocal1);
    operands[1].pushLength(method);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, lengthLocal2);
    method.getVisitor().visitMethodInsn(INVOKESTATIC, "java/lang/Math", "max", "(II)I", false);
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
    } else if(argumentType.equals(int.class)) {
      computeInt(method, integerNaLabel);
    } else {
      throw new UnsupportedOperationException();
    }
    cast(method.getVisitor(), applyMethod.getReturnType(), int.class);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return operands[0].mustCheckForIntegerNAs() || operands[1].mustCheckForIntegerNAs();
  }

  @Override
  public void appendToKey(StringBuilder key) {
    key.append(operatorName);
    key.append('(');
    for (LoopNode operandAccessor : operands) {
      operandAccessor.appendToKey(key);
      key.append(';');
    }
    key.append(')');
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> naIntegerLabel) {
    if (argumentType.equals(double.class)) {
      computeDouble(method, naIntegerLabel);
    } else if (argumentType.equals(int.class)) {
      computeInt(method, naIntegerLabel);
    } else {
      throw new UnsupportedOperationException(argumentType.getName());
    }
    cast(method.getVisitor(), applyMethod.getReturnType(), double.class);
  }


  private void computeInt(ComputeMethod method, Optional<Label> naLabel) {

    // If we've been asked to handle NA checking, then we have to set up our
    // internal NA handler block to handle the case that one of the arguments
    // is NA so that we can clean up the stack before jumping to the outer naLabel.

    // The Java bytecode verifier will not accept that multiple execution paths
    // arrive at the same point with different types on the stack.

    Optional<Label> argNaLabel = Optional.absent();
    if(naLabel.isPresent() &&
            (operands[0].mustCheckForIntegerNAs() || operands[1].mustCheckForIntegerNAs())) {
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

    operands[0].pushElementAsInt(method, argNaLabel);

    // stack => { index, value1 }
    mv.visitInsn(SWAP);
    // stack => { value1, index}
    mv.visitVarInsn(ILOAD, lengthLocal2);
    // stack => { value1, index, length2 }
    mv.visitInsn(IREM);
    // stack => { value1, index2 }

    operands[1].pushElementAsInt(method, argNaLabel);
    // stack => { value1, value2}

    mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(applyMethod.getDeclaringClass()),
            applyMethod.getName(),
            Type.getMethodDescriptor(applyMethod), false);

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

  private void computeDouble(ComputeMethod method, Optional<Label> integerNaLabel) {


    // If we've been asked to handle NA checking, then it gets even more complicated
    // than above because the stack will look different depending on which argument is NA,
    // because the double value of the first operand we push onto the stack requires
    // two positions on the stack.


    Optional<Label> argNaLabel1 = Optional.absent();
    if(integerNaLabel.isPresent() && operands[0].mustCheckForIntegerNAs()) {
      argNaLabel1 = Optional.of(new Label());
    }

    Optional<Label> argNaLabel2 = Optional.absent();
    if(integerNaLabel.isPresent() && operands[1].mustCheckForIntegerNAs()) {
      argNaLabel2 = Optional.of(new Label());
    }

    Optional<Label> done = Optional.absent();
    if(argNaLabel1.isPresent() || argNaLabel2.isPresent()) {
      done = Optional.of(new Label());
    }


    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP);
    mv.visitVarInsn(ILOAD, lengthLocal1);
    // stack => { index, index, length1 }
    mv.visitInsn(IREM);
    // stack => { index, index1 }
    
    operands[0].pushElementAsDouble(method, argNaLabel1);

    // stack => { index, [value1, value1] }
    mv.visitInsn(DUP2_X1); // next two instructions equivalent to swap
    mv.visitInsn(POP2);
    // stack => { value1, value1, index}
    mv.visitVarInsn(ILOAD, lengthLocal2);
    // stack => { value1, value1, index, length2 }
    mv.visitInsn(IREM);
    // stack => { value1, value1, index2 }
    operands[1].pushElementAsDouble(method, argNaLabel2);
    // stack => { value1, value2}


    mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(applyMethod.getDeclaringClass()),
            applyMethod.getName(),
            Type.getMethodDescriptor(applyMethod), false);

    if(done.isPresent()) {
      mv.visitJumpInsn(GOTO, done.get());
    }

    if(argNaLabel1.isPresent()) {
      mv.visitLabel(argNaLabel1.get());
      // upon arriving here, the stack contains
      // { index, value1::int } if is.na(arg1)
      // get rid of one of the ints
      // so that we jump to the outer na block, there is exactly
      // one extra int on the stack as expected.
      mv.visitInsn(POP);
      mv.visitJumpInsn(GOTO, integerNaLabel.get());
    }

    if(argNaLabel2.isPresent()) {
      mv.visitLabel(argNaLabel2.get());
      
      // upon arriving here, the stack contains
      // {value1, value1}, value2::int if is.na(arg2)
      // because the first value has already been converted to a double,
      // which occupies two slots on the stack
      mv.visitInsn(POP);
      mv.visitInsn(POP2);
      mv.visitInsn(ICONST_0);
      mv.visitJumpInsn(GOTO, integerNaLabel.get());
    }
    

    if(done.isPresent()) {
      mv.visitLabel(done.get());
    }
  }

  @Override
  public String toString() {
    return "(" + operands[0] + operatorName +  operands[1] + ")";
  }
}
