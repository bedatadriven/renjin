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


import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GimpleNode {
  private GimpleFunction function;
  private GimpleStatement statement;

  final List<GimpleNode> predecessors = new ArrayList<>();
  final List<GimpleNode> successors = new ArrayList<>();
  final Collection<GimpleFunction> callees = new ArrayList<>(0);

  public GimpleNode(GimpleFunction function, GimpleStatement statement) {
    this.function = function;
    this.statement = statement;
  }

  public boolean isStart() {
    return predecessors.isEmpty();
  }

  public GimpleFunction getFunction() {
    return function;
  }

  public GimpleStatement getStatement() {
    return statement;
  }

  public boolean isCall() {
    return !callees.isEmpty();
  }

  public Collection<GimpleFunction> getCallees() {
    return callees;
  }

  public Collection<GimpleNode> getPredecessors() {
    return predecessors;
  }

  public List<GimpleNode> getSuccessors() {
    return successors;
  }
}
