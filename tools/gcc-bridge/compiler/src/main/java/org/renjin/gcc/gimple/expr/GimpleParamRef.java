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
import org.renjin.gcc.gimple.GimpleParameter;
import java.util.function.Predicate;

public class GimpleParamRef extends GimpleLValue implements GimpleSymbolRef {

  private long id;
  private String name;

  public GimpleParamRef() {
  }

  public GimpleParamRef(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public GimpleParamRef(GimpleParameter parameter) {
    this.id = parameter.getId();
    this.name = parameter.getName();
    setType(parameter.getType());
  }

  public String getName() {
    return name;
  }

  @Override
  public String getMangledName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
  
  @Override
  public String toString() {
    return name;
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitParamRef(this);
  }
}
