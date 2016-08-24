package org.renjin.gcc.gimple.type;

import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimpleFunctionType that = (GimpleFunctionType) o;
    return Objects.equals(variableArguments, that.variableArguments) &&
           Objects.equals(returnType, that.returnType) &&
           Objects.equals(argumentTypes, that.argumentTypes);
  }

  @Override
  public int hashCode() {
    int result = returnType != null ? returnType.hashCode() : 0;
    result = 31 * result + (argumentTypes != null ? argumentTypes.hashCode() : 0);
    result = 31 * result + (variableArguments ? 1 : 0);
    return result;
  }
}
