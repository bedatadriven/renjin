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
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.Symbol;

import static org.junit.Assert.assertTrue;


public class ByteCodeEmitterTest {

  private Session session;

  @Before
  public void setup() {
    session = new SessionBuilder().build();
  }

  @Test
  public void simpleTest() throws IllegalAccessException, InstantiationException {
    ExpressionVector bodySexp = RParser.parseSource("s <- 0; for(i in 1:1000) { s <- sqrt(i) }; s;");
    //ExpressionVector bodySexp = RParser.parseSource("1+2;");

    IRBodyBuilder bodyBuilder = new IRBodyBuilder(session.getTopLevelContext(), session.getGlobalEnvironment());
    IRBody bodyIr = bodyBuilder.build(bodySexp);

    EnvironmentVariable s = bodyBuilder.getEnvironmentVariable(Symbol.get("s"));

    ControlFlowGraph cfg = new ControlFlowGraph(bodyIr);
    cfg.dumpGraph();

//    System.out.println(cfg);

    DominanceTree dTree = new DominanceTree(cfg);
    dTree.dumpGraph();

    SsaTransformer ssaTransformer = new SsaTransformer(cfg, dTree);
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
