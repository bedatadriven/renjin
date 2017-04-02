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
package org.renjin.gcc.gimple;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.guava.base.Predicate;

import java.util.Collections;
import java.util.List;

/**
 * Gimple Variable Declaration
 */
public class GimpleVarDecl implements GimpleDecl {
  private int id;
  private GimpleType type;
  private String name;
  private String mangledName;
  private GimpleExpr value;
  private GimpleCompilationUnit unit;
  private boolean global;
  
  @JsonProperty("const")
  private boolean constant;
  
  private boolean extern;
  
  private boolean weak;

  @JsonProperty("static")
  private boolean _static;

  /**
   * True if this local variable is addressable
   */
  private boolean addressable;

  public GimpleVarDecl() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public GimpleType getType() {
    return type;
  }

  public void setType(GimpleType type) {
    this.type = type;
  }

  public String getName() {
    if (name != null) {
      return name;
    } else {
      return "T" + Math.abs(id);
    }
  }
  
  public String getNameIfPresent() {
    return name;
  }

  public String getMangledName() {
    return mangledName;
  }

  public void setMangledName(String mangledName) {
    this.mangledName = mangledName;
  }

  public boolean isNamed() {
    return name != null;
  }

  public boolean isConstant() {
    return constant;
  }

  public void setConstant(boolean constant) {
    this.constant = constant;
  }

  public void setName(String name) {
    this.name = name;
  }

  public GimpleExpr getValue() {
    return value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  public boolean isAddressable() {
    return addressable;
  }

  public void setAddressable(boolean addressable) {
    this.addressable = addressable;
  }

  public GimpleCompilationUnit getUnit() {
    return unit;
  }

  public void setUnit(GimpleCompilationUnit unit) {
    this.unit = unit;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder().append(type).append(" ").append(getName());
    if(value != null) {
      s.append(" = ").append(value);
    }
    return s.toString();
  }

  /**
   * 
   * @return true f this variable declaration has external linkage, that is, it is visible outside
   * of the compilation unit.
   */
  public boolean isExtern() {
    return extern;
  }

  public void setExtern(boolean extern) {
    this.extern = extern;
  }

  public boolean isWeak() {
    return weak;
  }

  public void setWeak(boolean weak) {
    this.weak = weak;
  }

  public boolean isStatic() {
    return _static;
  }

  public void setStatic(boolean _static) {
    this._static = _static;
  }

  public Predicate<GimpleExpr> isReference() {
    return new Predicate<GimpleExpr>() {
      @Override
      public boolean apply(GimpleExpr input) {
        return input instanceof GimpleSymbolRef && 
            ((GimpleSymbolRef) input).getId() == id;
      }
    };
  }

  public GimpleVariableRef newRef() {
    return new GimpleVariableRef(id, type);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    if(value != null) {
      value.accept(visitor);
    }
  }

  public boolean isGlobal(){
    return global;
  }

  public void setGlobal(boolean global) {
    this.global = global;
  }
}
