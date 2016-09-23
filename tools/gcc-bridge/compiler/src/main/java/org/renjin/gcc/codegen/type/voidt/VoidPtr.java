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
package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.repackaged.asm.Label;


public class VoidPtr implements RefPtrExpr {
  
  private JExpr objectRef;
  private FatPtr address;

  public VoidPtr(JExpr objectRef, FatPtr address) {
    this.objectRef = objectRef;
    this.address = address;
  }

  public VoidPtr(JExpr objectRef) {
    this.objectRef = objectRef;
    this.address = null;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    JLValue lhs = (JLValue) this.objectRef;

    if(rhs instanceof FatPtrPair) {
      FatPtrPair fatPtrExpr = (FatPtrPair) rhs;
      lhs.store(mv, fatPtrExpr.wrap());
    } else {
      lhs.store(mv, ((RefPtrExpr) rhs).unwrap());
    }
  }
  
  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new NotAddressableException();
    }
    return address;
  }

  @Override
  public JExpr unwrap() {
    return objectRef;
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    objectRef.load(mv);
    mv.ifnull(label);
  }

  @Override
  public GExpr valueOf() {
    throw new UnsupportedOperationException("void pointers cannot be dereferenced.");
  }
}
