package org.renjin.gcc.gimple.type;

import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

public class GimpleFunctionType extends AbstractGimpleType {
  private GimpleType returnType;
  private List<GimpleType> argumentTypes = Lists.newArrayList();
  private boolean variableArguments;
  
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


  @Override
  public int sizeOf() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return returnType + " (*functionPtr)(" + Joiner.on(", ").join(argumentTypes) + ")";
  }
}
