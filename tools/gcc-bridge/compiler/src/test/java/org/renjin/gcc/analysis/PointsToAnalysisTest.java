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

import java.io.IOException;
import java.util.Arrays;

public class PointsToAnalysisTest extends AbstractGccTest {

  @Test
  public void test() throws IOException {

    GimpleCompilationUnit unit = compileToGimple("ide_solve.c");
    GimpleInterproceduralCFG cfg = new GimpleInterproceduralCFG(Arrays.asList(unit));
    PointsToAnalysis finder = new PointsToAnalysis(cfg);
    IFDSSolver<GimpleNode, PointsTo, GimpleFunction, GimpleInterproceduralCFG> solver = new IFDSSolver<>(finder);

    solver.solve();

    AllocationProfileMap profileMap = new AllocationProfileMap(cfg, solver);

  }

}