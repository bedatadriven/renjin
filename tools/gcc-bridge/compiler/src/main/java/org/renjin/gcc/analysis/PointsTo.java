/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleStringConstant;

public class PointsTo {

  public static final PointsTo ZERO = new PointsTo(new GimpleStringConstant(), new Allocation());

  private final GimpleExpr expr;
  private final Allocation allocation;
  private final int offset;

  public PointsTo(GimpleExpr expr, Allocation allocation) {
    this(expr, allocation, 0);
  }

  public PointsTo(GimpleExpr expr, Allocation allocation, int offset) {
    this.expr = expr;
    this.allocation = allocation;
    this.offset = offset;
  }

  public GimpleExpr getExpr() {
    return expr;
  }

  public Allocation getAllocation() {
    return allocation;
  }

  public int getOffset() {
    return offset;
  }

  public boolean isOffsetConstant() {
    return offset != -1;
  }

  public PointsTo withOffset(GimpleExpr lhs, GimpleExpr offset) {

    if(this.isOffsetConstant() && offset instanceof GimpleIntegerConstant) {
      int constantOffset = ((GimpleIntegerConstant) offset).getNumberValue().intValue();
      return new PointsTo(lhs, this.allocation, this.offset + constantOffset);
    }

    return new PointsTo(lhs, this.allocation, -1);

  }

  @Override
  public String toString() {
    if(this == ZERO) {
      return "{Ø}";
    } else if (offset == 0) {
      return "{" + expr + "=" + allocation + "}";
    } else if (offset == -1) {
      return "{" + expr + "=" + allocation + "+?}";
    } else {
      return "{" + expr + "=" + allocation + "+" + offset + "}";
    }
  }

}
