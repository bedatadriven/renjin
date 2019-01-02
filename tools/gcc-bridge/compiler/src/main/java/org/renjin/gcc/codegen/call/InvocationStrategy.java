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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.VariadicStrategy;
import org.renjin.repackaged.asm.Handle;

import java.util.List;

/**
 * Defines a strategy for invoking a JVM method
 */
public interface InvocationStrategy {

  /**
   * 
   * @return a JVM method handle that can be used to construct a function pointer
   */
  Handle getMethodHandle();
  
  List<ParamStrategy> getParamStrategies();

  VariadicStrategy getVariadicStrategy();


  ReturnStrategy getReturnStrategy();

  /**
   * Generates the bytecode instructions to invoke the method. 
   * 
   * <p>The method's parameters must already be on the stack, and after the generated instructions,
   * the method's return value, if any will be on the stack.</p>
   */
  void invoke(MethodGenerator mv);

}
