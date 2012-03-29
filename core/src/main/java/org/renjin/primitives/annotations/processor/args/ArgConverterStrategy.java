package org.renjin.primitives.annotations.processor.args;

import org.renjin.primitives.annotations.processor.JvmMethod;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;
import org.renjin.sexp.SEXP;


/**
 * Base class for the different strategies for converting incoming argument (SEXPs) to
 * the types declared in the java method.
 * 
 * @author alex
 *
 */
public abstract class ArgConverterStrategy {

  protected final JvmMethod.Argument formal;
  
  public ArgConverterStrategy(Argument formal) {
    super();
    this.formal = formal;
  }
  
  public Class getTempLocalType() {
    return formal.getClazz();
  }
  
  public Class getArgType() {
    return formal.getClazz();
  }
  
  /**
   * 
   * 
   * @param formal the formal argument declared in the jvm method
   * @param argumentExpression the java (source) expression that results in an argument of type {@code SEXP}
   * @return java source expression that results in the converted value
   */
  public abstract String conversionExpression(String argumentExpression);
  
  
  public String conversionStatement(String tempLocal, String argumentExpression) {
    return tempLocal + " = " + conversionExpression(argumentExpression) + ";";
  }
  
  public final String argConversionStatement(String tempLocal, String sexpExpr) {
    if(formal.getClazz().equals(SEXP.class)) {
      return tempLocal + " = " + sexpExpr + ";"; 
    } else {
      return conversionStatement(tempLocal, sexpExpr);
    }
  }
  
  public final String argConversionStatement(String tempLocal) {
    return argConversionStatement(tempLocal, extractExpression());
  }


  public String extractExpression() {
    if(formal.isEvaluated()) {
      return "argIt.next().evaluate(context, rho)";
    } else {
      return "argIt.next()";
    }
  }


  public boolean isEvaluated() {
    return formal.isEvaluated();
  }


  public abstract String getTestExpr(String argLocal);

}
