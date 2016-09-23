/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
  private Class<?> operandType;

  public BinaryVectorOpAccessor(DeferredNode node, InputGraph inputGraph) {
    this.operandAccessors[0] = Accessors.create(node.getOperands().get(0), inputGraph);
    this.operandAccessors[1] = Accessors.create(node.getOperands().get(1), inputGraph);
    applyMethod = findStaticApply(node.getVector());
    assert applyMethod != null;

    operandType = applyMethod.getParameterTypes()[0];
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
            method.getParameterTypes()[0].equals(method.getParameterTypes()[1]) ) {
          return method;
        }
      }
    }
    return null;
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


  private void pushDoubleArguments(ComputeMethod method) {
    
    // At this point:
    // The index must be the top of the stack
    
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP); // keep the index on the stack for the next operand
    
    mv.visitVarInsn(ILOAD, lengthLocal1);
    // stack => { index, index, length1 }
    mv.visitInsn(IREM);
    // stack => { index, index1 }
    operandAccessors[0].pushDouble(method);
    
    // stack => { index, [value1, value1] }
    mv.visitInsn(DUP2_X1); // next two instructions equivalent to swap
    mv.visitInsn(POP2);
    // stack => { value1, index}
    mv.visitVarInsn(ILOAD, lengthLocal2);
    // stack => { value1, index, length2 }
    mv.visitInsn(IREM);
    // stack => { value1, index2 }
    operandAccessors[1].pushDouble(method);
    // stack => { value1, value2}

  }

  private void pushIntArguments(ComputeMethod method) {

    // At this point:
    // The index must be the top of the stack

    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP); // keep the index on the stack for the next operand

    mv.visitVarInsn(ILOAD, lengthLocal1);
    // stack => { index, index, length1 }
    mv.visitInsn(IREM);
    // stack => { index, index1 }
    operandAccessors[0].pushInt(method);

    // stack => { index, value1 }
    mv.visitInsn(SWAP);

    // stack => { value1, index}
    mv.visitVarInsn(ILOAD, lengthLocal2);
    // stack => { value1, index, length2 }
    mv.visitInsn(IREM);
    // stack => { value1, index2 }
    operandAccessors[1].pushInt(method);
    // stack => { value1, value2}
  }
  
  private void pushComputation(ComputeMethod method) {
    if(operandType.equals(double.class)) {
      pushDoubleArguments(method);
    } else if(operandType.equals(int.class)) {
      pushIntArguments(method);
    } else {
      throw new IllegalStateException("operandType: " + operandType);
    }

    method.getVisitor().visitMethodInsn(INVOKESTATIC,
        Type.getInternalName(applyMethod.getDeclaringClass()),
        applyMethod.getName(),
        Type.getMethodDescriptor(applyMethod), false);
  }


  @Override
  public void pushDouble(ComputeMethod method) {
    pushComputation(method);
    
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
    pushComputation(method);

    if(applyMethod.getReturnType().equals(int.class)) {
      // NOOP
    } else if(applyMethod.getReturnType().equals(double.class)) {
      method.getVisitor().visitInsn(Opcodes.D2I);
    } else {
      throw new UnsupportedOperationException("returnType: " + applyMethod.getReturnType());
    }  
  }
}
