package r.jvmi.wrapper.generator.args;

import r.jvmi.binding.JvmMethod;

/**
 * Base class for the different strategies for converting incoming argument (SEXPs) to
 * the types declared in the java method.
 * 
 * @author alex
 *
 */
public abstract class ArgConverterStrategy {

  /**
   * 
   * @param formal the formal argument declared in the JVM method
   * @return true if this strategy cand handle the conversion
   */
  public abstract boolean accept(JvmMethod.Argument formal);
  
  /**
   * 
   * 
   * @param formal the formal argument declared in the jvm method
   * @param argumentExpression the java (source) expression that results in an argument of type {@code SEXP}
   * @return java source expression that results in the converted value
   */
  public abstract String convert(JvmMethod.Argument formal, String argumentExpression);
  
}
