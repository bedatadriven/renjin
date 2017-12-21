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
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

import javax.annotation.Nonnull;

public class ConstantValue implements JExpr {

  private Number value;
  private Type type;

  public ConstantValue(Type type, Number value) {
    this.type = type;
    this.value = value;
  }

  public Number getValue() {
    return value;
  }

  public int getIntValue() {
    Preconditions.checkState(type.equals(Type.INT_TYPE));
    
    return value.intValue();
  }
  
  @Nonnull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    switch (type.getSort()) {
      case Type.FLOAT:
        mv.fconst(value.floatValue());
        break;
      case Type.DOUBLE:
        mv.dconst(value.doubleValue());
        break;
      case Type.LONG:
        mv.lconst(value.longValue());
        break;
      case Type.BOOLEAN:
      case Type.BYTE:
      case Type.SHORT:
      case Type.CHAR:
      case Type.INT:
        mv.iconst(value.intValue());
        break;
      default:
        throw new UnsupportedOperationException("type: " + type);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConstantValue that = (ConstantValue) o;

    if (!value.equals(that.value)) {
      return false;
    }
    return type.equals(that.type);

  }

  @Override
  public int hashCode() {
    int result = value.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }

  public static boolean isZero(JExpr expr) {
    if(expr instanceof ConstantValue) {
      ConstantValue constantValue = (ConstantValue) expr;
      if(constantValue.getIntValue() == 0) {
        return true;
      }
    }
    return false;
  }
}
