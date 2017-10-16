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

import heros.solver.IFDSSolver;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllocationProfileMap {

  private final GimpleInterproceduralCFG cfg;
  private final IFDSSolver<GimpleNode, PointsTo, GimpleFunction, GimpleInterproceduralCFG> solver;
  private final Map<Allocation, AllocationProfile> map = new HashMap<>();

  public AllocationProfileMap(
      GimpleInterproceduralCFG cfg,
      IFDSSolver<GimpleNode, PointsTo, GimpleFunction, GimpleInterproceduralCFG> solver) {

    this.cfg = cfg;
    this.solver = solver;

    for (final GimpleNode node : cfg.getNodes()) {
    }
  }

  private void addRead(GimpleNode node, GimpleExpr pointer, GimpleExpr offset, GimpleType type) {
    Set<Allocation> allocations = findAllocation(node, pointer);
    for (Allocation allocation : allocations) {
      addRead(allocation, offset, type);
    }
  }

  private void addRead(Allocation allocation, GimpleExpr offset, GimpleType type) {
    AllocationProfile profile = map.get(allocation);
    if(profile == null) {
      profile = new AllocationProfile(allocation);
    }
    profile.addRead(type);
  }

  private Set<Allocation> findAllocation(GimpleNode node, GimpleExpr pointer) {
    Set<PointsTo> facts = solver.ifdsResultsAt(node);
    Set<Allocation> set = new HashSet<>();
    for (PointsTo fact : facts) {
      if(fact.getExpr().equals(pointer)) {
        set.add(fact.getAllocation());
      }
    }
    return set;
  }
}
