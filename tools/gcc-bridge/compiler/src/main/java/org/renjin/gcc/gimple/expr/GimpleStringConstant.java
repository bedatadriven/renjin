/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;
import java.util.function.Predicate;

public class GimpleStringConstant extends GimpleConstant {

  private String value;
  
  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public void setType(GimpleType type) {
    if(!(type instanceof GimpleArrayType)) {
      throw new RuntimeException("Expected array type for StringConstant, got: " + type);
    }
    super.setType(type);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitStringConstant(this);
  }

  @Override
  public GimpleArrayType getType() {
    return (GimpleArrayType) super.getType();
  }

  @Override
  public String toString() {
    return "\"" + value.replace("\u0000", "<NULL>") + "\"";
  }
}
