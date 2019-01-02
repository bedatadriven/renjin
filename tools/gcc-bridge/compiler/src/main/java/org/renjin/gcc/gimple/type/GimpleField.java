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
package org.renjin.gcc.gimple.type;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.repackaged.guava.base.Strings;

public class GimpleField {
  private long id;
  private int offset;
  private int size;
  private String name;
  private GimpleType type;
  private boolean addressed;
  private boolean referenced;

  public GimpleField() {
  }

  public GimpleField(String name, GimpleType type) {
    this.name = name;
    this.type = type;
  }

  public GimpleField(String name, GimpleType type, int offset) {
    this.offset = offset;
    this.size = type.getSize();
    this.name = name;
    this.type = type;
  }

  public boolean isAddressed() {
    return addressed;
  }

  public void setAddressed(boolean addressed) {
    this.addressed = addressed;
  }

  public boolean isReferenced() {
    return referenced;
  }

  public void setReferenced(boolean referenced) {
    this.referenced = referenced;
  }

  public String getName() {
    return Strings.nullToEmpty(name);
  }
  public void setName(String name) {
    this.name = name;
  }
  public GimpleType getType() {
    return type;
  }
  public void setType(GimpleType type) {
    this.type = type;
  }


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getOffset() {
    return offset;
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

  public boolean hasName() {
    return name != null;
  }

  @Override
  public String toString() {
    return "GimpleField[" + (addressed ? "&" : "") + name + ":" + type + "]";
  }

  public GimpleExpr refTo() {
    return new GimpleFieldRef(name, offset, type);
  }
}
