package org.renjin.gcc.gimple.type;


import com.google.common.base.Joiner;

import java.util.Collections;
import java.util.List;

public class FunctionPointerType implements GimpleType {

  private final GimpleType returnType;
  private final List<GimpleType> arguments;

  public FunctionPointerType(GimpleType returnType, List<GimpleType> arguments) {
    this.returnType = returnType;
    this.arguments = Collections.unmodifiableList(arguments);
  }

  public GimpleType getReturnType() {
    return returnType;
  }

  public List<GimpleType> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return returnType + " (*T) (" + Joiner.on(", ").join(arguments) + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FunctionPointerType that = (FunctionPointerType) o;

    if (!arguments.equals(that.arguments)) return false;
    if (!returnType.equals(that.returnType)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = returnType.hashCode();
    result = 31 * result + arguments.hashCode();
    return result;
  }
}
