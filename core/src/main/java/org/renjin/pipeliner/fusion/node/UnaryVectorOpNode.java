/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.sexp.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static org.renjin.repackaged.asm.Opcodes.INVOKESTATIC;

public class UnaryVectorOpNode extends LoopNode {

  private String operatorName;
  private LoopNode operand;
  private final Class<?> operandType;
  private Method applyMethod;
  private Class<?> returnType;

  public UnaryVectorOpNode(String name, Method operator, LoopNode operand) {
    this.operatorName = name;

    applyMethod = operator;
    assert applyMethod != null;

    this.operandType = applyMethod.getParameterTypes()[0];
    this.operand = operand;
    
    returnType = applyMethod.getReturnType();
  }


  public static Method findMethod(Vector vector) {
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
    operand.init(method);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    operand.pushLength(method);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return operand.mustCheckForIntegerNAs();
  }

  @Override
  public void appendToKey(StringBuilder key) {
    key.append(operatorName);
    key.append('(');
    operand.appendToKey(key);
    key.append(')');
  }

  private void pushResult(ComputeMethod method, Optional<Label> integerNaLabel) {
    if (operandType.equals(double.class)) {
      operand.pushElementAsDouble(method, integerNaLabel);

    } else if(operandType.equals(int.class)) {
      operand.pushElementAsInt(method, integerNaLabel);

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

  @Override
  public String toString() {
    return operatorName + "(" + operand + ")";
  }

}
