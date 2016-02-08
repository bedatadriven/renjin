package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.var.Value;

import java.util.List;

/**
 * Methods that operate on {@link ValueFunction}s
 */
public class ValueFunctions {
  
  public static List<Value> getConstructorsChecked(ValueFunction valueFunction) {

    List<Value> initialValues = valueFunction.getDefaultValue();
    
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
