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
package org.renjin.gcc.gimple.statement;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleLabel;
import org.renjin.gcc.gimple.GimpleVisitor;

import java.util.Collections;
import java.util.Set;

public class GimpleGoto extends GimpleStatement {
  private int target;

  public GimpleLabel getTargetLabel() {
    return new GimpleLabel("bb" + target);
  }

  public int getTarget() {
    return target;
  }

  public void setTarget(int target) {
    this.target = target;
  }

  @Override
  public String toString() {
    return "goto " + getTargetLabel();
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitGoto(this);

  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    
  }

  @Override
  public Set<Integer> getJumpTargets() {
    return Collections.singleton(target);
  }
}
