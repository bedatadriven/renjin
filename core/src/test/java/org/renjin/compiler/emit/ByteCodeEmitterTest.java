package org.renjin.compiler.emit;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.TypeResolver;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.Symbol;

import java.util.Map;

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

    VariableMap variableMap = new VariableMap(cfg);

    SsaVariable s2 = s.getVersion(2);
    assertTrue(variableMap.isDefined(s2));
    assertTrue(variableMap.isUsed(s2));

    TypeResolver typeResolver = new TypeResolver();
    typeResolver.resolveType(cfg, variableMap);

    ssaTransformer.removePhiFunctions(variableMap);

    System.out.println(cfg);

    ByteCodeEmitter emitter = new ByteCodeEmitter(cfg);
    CompiledBody compiledBody = emitter.compile().newInstance();

    System.out.println(compiledBody.evaluate(session.getTopLevelContext(), session.getGlobalEnvironment()));
  }
}
