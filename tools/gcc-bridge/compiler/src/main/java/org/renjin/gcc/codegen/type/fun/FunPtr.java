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
package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.expr.RefPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Label;


public class FunPtr implements RefPtrExpr {

  private JExpr methodHandleExpr;
  private FatPtr address;

  public FunPtr(JExpr methodHandleExpr) {
    this.methodHandleExpr = methodHandleExpr;
    this.address = null;
  }

  public FunPtr(JExpr methodHandleExpr, FatPtr address) {
    this.methodHandleExpr = methodHandleExpr;
    this.address = address;
  }

  public JExpr unwrap() {
    return methodHandleExpr;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    FunPtr rhsFunPtrExpr = (FunPtr) rhs;
    ((JLValue) methodHandleExpr).store(mv, rhsFunPtrExpr.methodHandleExpr);
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new InternalCompilerException("Not addressable");
    }
    return address;
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    methodHandleExpr.load(mv);
    mv.ifnull(label);
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    return this;
  }
}
