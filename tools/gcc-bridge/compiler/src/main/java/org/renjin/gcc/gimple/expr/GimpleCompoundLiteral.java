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
import java.util.function.Predicate;

import java.util.List;

/**
 * A literal record value. 
 * 
 * <p><a href="https://gcc.gnu.org/onlinedocs/gcc/Compound-Literals.html">Compound Literals</a> are translated
 * to gimple by creating a new global variable initialized to the value of the literal, and replacing the 
 * references to the literal with references to the global variable.
 * </ul>
 * 
 */
public class GimpleCompoundLiteral extends GimpleLValue {

  private GimpleVariableRef decl;

  /**
   * 
   * @return the reference to the global variable initialized to the value of the literal
   */
  public GimpleVariableRef getDecl() {
    return decl;
  }

  public void setDecl(GimpleVariableRef decl) {
    this.decl = decl;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    decl.find(predicate, results);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    decl = (GimpleVariableRef) replaceOrDescend(decl, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitCompoundLiteral(this);
  }

  @Override
  public String toString() {
    return "GimpleCompoundLiteral{" +
        "decl=" + decl +
        '}';
  }
}
