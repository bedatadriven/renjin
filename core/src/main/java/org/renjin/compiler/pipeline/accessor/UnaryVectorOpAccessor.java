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


  private void push(ComputeMethod method) {
    if (operandType.equals(double.class)) {
      operandAccessor.pushDouble(method);
    } else if(operandType.equals(int.class)) {
      operandAccessor.pushInt(method);
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
  public void pushDouble(ComputeMethod method) {
    push(method);

    if(returnType.equals(int.class)) {
      method.getVisitor().visitInsn(Opcodes.I2D);
    } else if(returnType.equals(double.class)) {
      // NOOP 
    } else {
      throw new UnsupportedOperationException("returnType: " + returnType);
    }
  }

  @Override
  public void pushInt(ComputeMethod method) {
    push(method);

    if(returnType.equals(int.class)) {
      // NOOP 
    } else if(returnType.equals(double.class)) {
      method.getVisitor().visitInsn(Opcodes.D2I);
    } else {
      throw new UnsupportedOperationException("returnType: " + returnType);
    }
  }
}
