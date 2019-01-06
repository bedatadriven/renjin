/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import java.util.function.Predicate;

public class GimpleFunctionRef extends GimpleExpr {
  private long id;
  private String name;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public GimpleFunctionRef() {
  }

  public GimpleFunctionRef(String name) {
    this.name = name;
    setType(new GimpleFunctionType());
  }

  @Override
  public String toString() {
    if (name != null) {
      return name;
    } else {
      return "T" + Math.abs(id);
    }
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    // NOOP: No sub expressions
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitFunctionRef(this);
  }
}
