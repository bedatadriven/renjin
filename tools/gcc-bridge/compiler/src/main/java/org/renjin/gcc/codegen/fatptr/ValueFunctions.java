package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.expr.SimpleExpr;

import java.util.List;

/**
 * Methods that operate on {@link ValueFunction}s
 */
public class ValueFunctions {
  
  public static List<SimpleExpr> getConstructorsChecked(ValueFunction valueFunction) {

    List<SimpleExpr> initialValues = valueFunction.getDefaultValue();
    
    if(initialValues.isEmpty()) {
      return initialValues;
    }
    
    if(initialValues.size() != valueFunction.getElementLength()) {
      throw new IllegalStateException(String.format(
          "Inconsistent value function: valueFunction.getElementLength() = %d, but " +
              "valueFunction.getDefaultValue().size() = %d",
          initialValues.size(),
          valueFunction.getElementLength()));
    }
   
    return initialValues;
  }
}
