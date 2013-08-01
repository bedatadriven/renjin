package org.renjin.compiler.emit;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.ir.ssa.SsaTransformer;
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
    DominanceTree dTree = new DominanceTree(cfg);

    SsaTransformer ssaTransformer = new SsaTransformer(cfg, dTree);
    ssaTransformer.transform();
    
    System.out.println(cfg);
    
    VariableMap variableMap = new VariableMap(cfg);
    
    ssaTransformer.removePhiFunctions(variableMap);
    
    ByteCodeEmitter emitter = new ByteCodeEmitter(cfg);
    CompiledBody compiledBody = emitter.compile().newInstance();
                              
    System.out.println(compiledBody.evaluate(session.getTopLevelContext(), session.getGlobalEnvironment()));

  }


}
