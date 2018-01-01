/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.gimple.type;

import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;
import java.util.Objects;

public class GimpleFunctionType extends AbstractGimpleType {
  private GimpleType returnType;
  private List<GimpleType> argumentTypes = Lists.newArrayList();
  private boolean variableArguments;

  public GimpleFunctionType() {
    returnType = new GimpleVoidType();
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
