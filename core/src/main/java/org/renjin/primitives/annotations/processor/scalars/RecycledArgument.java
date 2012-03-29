package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.primitives.annotations.processor.JvmMethod;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;

public class RecycledArgument {
  
  
  /**
   * The formal definition of this argument
   */
  private JvmMethod.Argument formal;
  
  /**
   * the local variable in which the vector 
   * is stored
   */
  private String vectorLocal;
  
  
  private ScalarType scalarType;

  public RecycledArgument(Argument formal, String localVariable) {
    super();
    this.formal = formal;
    this.vectorLocal = localVariable;
    this.scalarType = ScalarTypes.get(formal.getClazz());
  }

  public JvmMethod.Argument getFormal() {
    return formal;
  }

  public String getVectorLocal() {
    return vectorLocal;
  }
  
  public String getElementLocal() {
    return vectorLocal + "_element";
  }
  
  public String getLengthLocal() {
    return vectorLocal + "_length";
  }

  public String getElementClassName() {
    return scalarType.getScalarType().getName();
  }
  
  public String getAccessExpression(String indexExpression) {
    return getVectorLocal() + "." + scalarType.getAccessorMethod() + 
        "(" + indexExpression + ")";
  }
}
