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
package org.renjin.compiler.codegen;

import org.junit.Before;
import org.junit.Test;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.cfg.UseDefMap;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.Symbol;

import static org.junit.Assert.assertTrue;


public class ClassGeneratorTest {

  private Session session;

  @Before
  public void setup() {
    session = new SessionBuilder().build();
  }

  @Test
  public void simpleTest() throws IllegalAccessException, InstantiationException {
    ExpressionVector bodySexp = RParser.parseSource("s <- 0; for(i in 1:1000) { s <- sqrt(i) }; s;");
    //ExpressionVector bodySexp = RParser.parseSource("1+2;");

    RuntimeState runtimeState = new RuntimeState(session.getTopLevelContext(), session.getGlobalEnvironment());
    IRBodyBuilder bodyBuilder = new IRBodyBuilder(runtimeState);
    IRBody bodyIr = bodyBuilder.build(bodySexp);

    EnvironmentVariable s = bodyBuilder.getEnvironmentVariable(Symbol.get("s"));

    ControlFlowGraph cfg = new ControlFlowGraph(bodyIr);
    cfg.dumpGraph();

//    System.out.println(cfg);

    DominanceTree dTree = new DominanceTree(cfg);
    dTree.dumpGraph();

    SsaTransformer ssaTransformer = new SsaTransformer(cfg);
    ssaTransformer.transform();

    System.out.println(cfg);

    UseDefMap useDefMap = new UseDefMap(cfg);
    TypeSolver types = new TypeSolver(cfg, useDefMap);

    SsaVariable s2 = s.getVersion(2);
    assertTrue(types.isDefined(s2));
    assertTrue(types.isUsed(s2));

    ssaTransformer.removePhiFunctions(types);

    System.out.println(cfg);
//
//    ByteCodeEmitter emitter = new ByteCodeEmitter(cfg);
//    CompiledBody compiledBody = emitter.compile().newInstance();
//
//    System.out.println(compiledBody.evaluate(session.getTopLevelContext(), session.getGlobalEnvironment()));
  }
}
