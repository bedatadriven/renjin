package org.renjin.compiler.emit;

import org.junit.Before;
import org.junit.Test;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.ir.ssa.RegisterAllocation;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.ssa.TypeResolver;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;


public class ByteCodeEmitterTest {

  private Session session;

  @Before
  public void setup() {
    session = new SessionBuilder().build();
  }

  @Test
  public void simpleTest() throws IllegalAccessException, InstantiationException {
    ExpressionVector bodySexp = RParser.parseSource("s <- 0; for(i in 1:1000) { s <- sqrt(i) }; s;");
    IRBodyBuilder bodyBuilder = new IRBodyBuilder(session.getTopLevelContext(), session.getGlobalEnvironment());
    IRBody bodyIr = bodyBuilder.build(bodySexp);

    ControlFlowGraph cfg = new ControlFlowGraph(bodyIr);
    cfg.dumpGraph();

    DominanceTree dTree = new DominanceTree(cfg);
    dTree.dumpGraph();

    SsaTransformer ssaTransformer = new SsaTransformer(cfg, dTree);
    ssaTransformer.transform();

   // System.out.println(cfg);

    VariableMap variableMap = new VariableMap(cfg);
    new TypeResolver(cfg, variableMap).resolveTypes();

    ssaTransformer.removePhiFunctions(variableMap);

    System.out.println(cfg);

    ByteCodeEmitter emitter = new ByteCodeEmitter(cfg);
    CompiledBody compiledBody = emitter.compile().newInstance();

    System.out.println(compiledBody.evaluate(session.getTopLevelContext(), session.getGlobalEnvironment()));
  }
}
