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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;

import java.util.List;
import java.util.Optional;

/**
 * Provides a strategy for passing Gimple values as JVM method parameters.
 *
 * <p>A {@code ParamStrategy} is composed of a two things:</p>
 *
 * <ul>
 *   <li>How the argument is represented, as one or more JVM arguments.</li>
 *   <li>How to marshal the gimple value onto the stack in advance of the method call.</li>
 *   <li>How to store or load the value of the parameter within a function body</li>
 * </ul>
 *
 * <p>The simplest strategy is the {@link org.renjin.gcc.codegen.type.primitive.PrimitiveParamStrategy}, which maps a Gimple argument
 * to a JVM argument of the corresponding type.</p>
 *
 * <p>However types like {@code complex} require more sophisticated handling: a single complex-valued argument
 * requires <em>two</em> JVM arguments: one for the real value and one for the imaginary.</p>
 *
 */
public interface ParamStrategy {

  /**
   * @return one or more JVM types used to represent this parameter.
   */
  List<Type> getParameterTypes();

  /**
   *
   * @param name a java-friendly name for each java parameter required for a parameter with the given
   *             {@code name}
   */
  List<String> getParameterNames(String name);

  /**
   * Emits any bytecode necessary to initialize the parameter at the start of the generated method body, 
   * and returns an ExprGenerator which can be used to retrieve its value
   *
   * @param methodVisitor MethodVisitor to write its value
   * @param paramVars the first index among the parameters
   * @param localVars an {@link LocalVarAllocator} which can be used to reserve additional local variable slots
   *                   if needed.
   * @return an {@code ExprGenerator} which can be used to access this parameter's value.
   */
  GExpr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars);


  /**
   * Pushes a value onto the stack in the format necessary for function parameter using this strategy.
   */
  void loadParameter(MethodGenerator mv, Optional<GExpr> argument);


}
