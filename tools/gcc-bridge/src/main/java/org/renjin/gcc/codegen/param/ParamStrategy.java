package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.Var;
import org.renjin.gcc.codegen.VarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
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
 * <p>The simplest strategy is the {@link PrimitiveParamStrategy}, which maps a Gimple argument
 * to a JVM argument of the corresponding type.</p>
 * 
 * <p>However types like {@code complex} require more sophisticated handling: a single complex-valued argument
 * requires <em>two</em> JVM arguments: one for the real value and one for the imaginary.</p>
 *
 */
public abstract class ParamStrategy {


  /**
   * @return number of local variable slots occupied by this parameter
   */
  public final int numSlots() {
    int size = 0;
    for (Type type : getParameterTypes()) {
      size += type.getSize();
    }
    return size;
  }

  /**
   * 
   * @return one or more JVM types used to represent this parameter.
   */
  public abstract List<Type> getParameterTypes();

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
  public abstract ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars);

  
  /**
   * Pushes a value onto the stack in the format neccessary for function paramter using this strategy.
   */
  public abstract void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator);


}
