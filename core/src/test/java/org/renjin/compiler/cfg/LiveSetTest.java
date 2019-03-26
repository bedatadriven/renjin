/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.cfg;

import org.junit.Test;
import org.renjin.compiler.CompilerTestCase;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.statements.Statement;

public class LiveSetTest extends CompilerTestCase {

  @Test
  public void test() {

    IRBody body = buildBody("x <- numeric(100); for(i in 1:100) x[i] <- i");
    ControlFlowGraph cfg = new ControlFlowGraph(body);
    cfg.dumpGraph();

    SsaTransformer transformer = new SsaTransformer(cfg);
    transformer.transform();

    System.out.println(cfg);
    
    UseDefMap useDefMap = new UseDefMap(cfg);
    
    LivenessCalculator liveSet = new LivenessCalculator(cfg, useDefMap);
    
    BasicBlock loopBody = cfg.get("BB3");
    Statement replaceStatement = loopBody.getStatements().get(1);
  
    // Replace statement should now be
    // x₃ ← ([<- i₂ i₂)
    System.out.println(replaceStatement);
   
    // Verify that x₂ is NOT live-out
    SsaVariable x2 = new SsaVariable(new EnvironmentVariable("x"), 2);

    liveSet.computeLiveOutSet(x2);



  }
  
}