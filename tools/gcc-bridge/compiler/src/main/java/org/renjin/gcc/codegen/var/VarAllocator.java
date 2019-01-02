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
package org.renjin.gcc.codegen.var;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.Optional;

/**
 * Common interface to generating code for local and global variables.
 *
 * @see LocalVarAllocator
 * @see GlobalVarAllocator
 */
public abstract class VarAllocator {


  public abstract JLValue reserve(String name, Type type);
  
  public abstract JLValue reserve(String name, Type type, JExpr initialValue);

  public final JLValue reserve(String name, Class type) {
    return reserve(name, Type.getType(type));
  }

  public final JLValue reserveArrayRef(String name, Type componentType) {
    return reserve(name, Type.getType("[" + componentType.getDescriptor()));
  }
  
  public final JLValue reserveUnitArray(String name, Type componentType, Optional<JExpr> initialValue) {

    JExpr newArray;
    if(initialValue.isPresent()) {
      newArray = Expressions.newArray(componentType, Collections.singletonList(initialValue.get()));
    } else {
      newArray = Expressions.newArray(componentType, 1);
    }
    return reserve(name, Type.getType("[" + componentType.getDescriptor()), newArray);
  }

  public final JLValue reserveInt(String name) {
    return reserve(name, Type.INT_TYPE);
  }
  
  public final JLValue reserveOffsetInt(String name) {
    if(name == null) {
      return reserve(null, Type.INT_TYPE);
    } else {
      return reserve(name + "$offset", Type.INT_TYPE);
    }
  }
  

  public static String toJavaSafeName(String name) {
    if(name.equals("this")) {
      return "_this";
    }
    return name.replace('.', '$');
  }

}
