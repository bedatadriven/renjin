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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.vptr.VPtrVariadicStrategy;
import org.renjin.repackaged.asm.Handle;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Invocation strategy for existing JVM static methods.
 */
public class StaticMethodStrategy implements InvocationStrategy {

  private final TypeOracle typeOracle;
  private Method method;

  /**
   * List of ParamStrategies, constructed lazily.
   */
  private List<ParamStrategy> paramStrategies;

  /**
   * Strategy for dealing with the method's return value, constructed lazily.
   */
  private ReturnStrategy returnStrategy;

  public StaticMethodStrategy(TypeOracle typeOracle, Method method) {
    this.typeOracle = typeOracle;
    this.method = method;
  }

  @Override
  public Handle getMethodHandle() {
    return new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), 
        method.getName(), Type.getMethodDescriptor(method));
  }

  @Override
  public List<ParamStrategy> getParamStrategies() {
    if(paramStrategies == null) {
      paramStrategies = typeOracle.forParameterTypesOf(method);
    }
    return paramStrategies;
  }

  @Override
  public VariadicStrategy getVariadicStrategy() {
    if(method.isVarArgs()) {
      return new JvmVarArgsStrategy();
    } else if(VPtrVariadicStrategy.hasVarArgsPtr(method)) {
      return new VPtrVariadicStrategy();
    } else {
      return new NullVariadicStrategy();
    }
  }


  @Override
  public ReturnStrategy getReturnStrategy() {
    if (returnStrategy == null) {
      returnStrategy = typeOracle.forReturnValue(method);
    }
    return returnStrategy;
  }
  
  @Override
  public void invoke(MethodGenerator mv) {
    mv.invokestatic(method.getDeclaringClass(), method.getName(), Type.getMethodDescriptor(method));
  }
}
