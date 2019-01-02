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
import org.renjin.gcc.gimple.type.GimpleType;
import java.util.function.Predicate;

/**
 * Gimple expression which evaluates to the name of a field
 */
public class GimpleFieldRef extends GimpleExpr {
  private long id;
  private int offset;
  private int size;
  private String name;

  public GimpleFieldRef() {
  }

  public GimpleFieldRef(String name, int offset, GimpleType type) {
    this.offset = offset;
    this.setType(type);
    this.size = type.getSize();
    this.name = name;
  }

  /**
   * 
   * @return the offset of this field in bits, from the start of the record.
   */
  public int getOffset() {
    return offset;
  }
  
  public int getOffsetBytes() {
    return offset / 8;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getName() {
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
    if(name == null) {
      return "field@" + offset;
    }
    if(name.contains(".")) {
      return "[" + name + "]";
    } 
    return name;
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    // NOOP: Leaf node
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitFieldRef(this);
  }
}
