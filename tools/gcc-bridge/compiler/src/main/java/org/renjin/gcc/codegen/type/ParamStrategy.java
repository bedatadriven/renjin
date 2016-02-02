package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.List;

/**
 * Provides a strategy for passing Gimple values as JVM method parameters.
 *
 * <p>A {@code ParamStrategy} is composed of a two things:</p>
 *
 * <ul>
 *   <li>How the argument is represented as one or more JVM arguments.</li>
 *   <li>How to marshal the gimple value onto the stack in advance of the method call.</li>
 *   <li>How to store or load the value of the parameter within a function body</li>
 * </ul>
 *
 * <p>The simplest strategy is the {@link ValueParamStrategy}, which maps a Gimple argument
 * to a JVM argument of the corresponding type.</p>
 *
 * <p>However types like {@code complex} require more sophisticated handling: a single complex-valued argument
 * requires <em>two</em> JVM arguments: one for the real value and one for the imaginary.</p>
 *
 */
public interface ParamStrategy {


  /**
   *
   * @return one or more JVM types used to represent this parameter.
   */
  List<Type> getParameterTypes();

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
  ExprGenerator emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars);


  /**
   * Pushes a value onto the stack in the format neccessary for function paramter using this strategy.
   */
  void emitPushParameter(MethodGenerator mv, ExprGenerator parameterValueGenerator);


}
