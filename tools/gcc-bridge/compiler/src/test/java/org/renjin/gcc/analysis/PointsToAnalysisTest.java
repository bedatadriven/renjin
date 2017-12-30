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
import org.junit.Test;
import org.renjin.gcc.AbstractGccTest;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class PointsToAnalysisTest extends AbstractGccTest {

  @Test
  public void heapAliasing() throws IOException {

    AllocationProfileMap profileMap = solve("points_to1.c");
    assertThat(profileMap.getAllocations(), hasSize(1));

    AllocationProfile heap = profileMap.getAllocations().iterator().next();
    assertThat(heap.getReads(), hasSize(0));
    assertThat(heap.getWrites(), contains((GimpleType)new GimpleRealType(64)));
  }


  @Test
  public void pointsToStack() throws IOException {
    AllocationProfileMap profileMap = solve("points_to2.c");
    assertThat(profileMap.getAllocations(), hasSize(2));
  }

  private AllocationProfileMap solve(String sourceFile) throws IOException {
    GimpleCompilationUnit unit = compileToGimple(sourceFile);
    GimpleInterproceduralCFG cfg = new GimpleInterproceduralCFG(Arrays.asList(unit));
    PointsToAnalysis finder = new PointsToAnalysis(cfg);
    IFDSSolver<GimpleNode, PointsTo, GimpleFunction, GimpleInterproceduralCFG> solver = new IFDSSolver<>(finder);

    solver.solve();

    return new AllocationProfileMap(cfg, solver);
  }

}