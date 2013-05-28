package org.renjin.gcc.gimple.type;

import java.util.List;

import com.google.common.collect.Lists;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public class GimpleFunctionType extends AbstractGimpleType {
  private GimpleType returnType;
  private int size;
  private List<GimpleType> argumentTypes = Lists.newArrayList();
  private boolean variableArguments;
  
  
  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public GimpleType getReturnType() {
    return returnType;
  }

  public void setReturnType(GimpleType returnType) {
    this.returnType = returnType;
  }

  public List<GimpleType> getArgumentTypes() {
    return argumentTypes;
  }

  public boolean isVariableArguments() {
    return variableArguments;
  }

  public void setVariableArguments(boolean variableArguments) {
    this.variableArguments = variableArguments;
  }
  
  
}
