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
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.guava.base.Predicate;

public class GimpleVariableRef extends GimpleLValue implements GimpleSymbolRef {

  private long id;
  private String name;
  private String mangledName;
  
  public GimpleVariableRef() {
  }

  public GimpleVariableRef(long id, GimpleType type) {
    this.id = id;
    this.setType(type);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getMangledName() {
    if(mangledName != null) {
      return mangledName;
    }
    return name;
  }

  public void setMangledName(String mangledName) {
    this.mangledName = mangledName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimpleVariableRef that = (GimpleVariableRef) o;

    if (id != that.id) {
      return false;
    }
    return !(name != null ? !name.equals(that.name) : that.name != null);
  }


  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
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
    // NOOP: Leaf node
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitVariableRef(this);
  }
}
