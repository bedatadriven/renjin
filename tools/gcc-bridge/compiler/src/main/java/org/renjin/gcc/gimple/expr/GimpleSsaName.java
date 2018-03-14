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
 * Gimple Single-Static Assignment (SSA) Variable name
 */
public class GimpleSsaName extends GimpleLValue {
  private GimpleExpr var;
  private int version;
  private boolean defaultDefinition;
  private boolean occursInAbnormalPhi;

  public GimpleExpr getVar() {
    return var;
  }

  public void setVar(GimpleExpr var) {
    this.var = var;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public boolean isDefaultDefinition() {
    return defaultDefinition;
  }

  public void setDefaultDefinition(boolean defaultDefinition) {
    this.defaultDefinition = defaultDefinition;
  }

  public boolean isOccursInAbnormalPhi() {
    return occursInAbnormalPhi;
  }

  public void setOccursInAbnormalPhi(boolean occursInAbnormalPhi) {
    this.occursInAbnormalPhi = occursInAbnormalPhi;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(var, predicate, results);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    var = replaceOrDescend(var, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitSsaName(this);
  }

  @Override
  public String toString() {
    if(defaultDefinition) {
      return var + toSubscript(0);
    } else {
      return var + toSubscript(version);
    }
  }
  
  private String toSubscript(int version) {
    String digits = Integer.toString(version);
    StringBuilder sb = new StringBuilder(digits.length());
    for(int i=0;i!=digits.length();++i) {
      int digit = digits.charAt(i) - '0';
      sb.appendCodePoint(0x2080 + digit);
    }
    return sb.toString();
  }
}
