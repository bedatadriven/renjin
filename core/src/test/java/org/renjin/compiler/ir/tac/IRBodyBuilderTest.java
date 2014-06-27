package org.renjin.compiler.ir.tac;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.renjin.EvalTestCase;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.invoke.ClassBindings;
import org.renjin.invoke.reflection.MemberBinding;
import org.renjin.parser.RParser;
import org.renjin.sexp.*;


public class IRBodyBuilderTest extends EvalTestCase {

  private Session session;

  public static void stop() {
    throw new EvalException("stop!");
  }

  @Before
  public void setUpSession() {
    session = new SessionBuilder().build();

    // add some simplified functions for testing
    MemberBinding stop = ClassBindings.getClassDefinitionBinding(IRBodyBuilderTest.class)
        .getMemberBinding(Symbol.get("stop"));
    session.getGlobalEnvironment().setVariable("stop", stop.getValue(null));
  }

  @Test
  public void simple() {
    ExpressionVector ast = RParser.parseSource("x + sqrt(x * y)\n");
    IRBodyBuilder factory = newBodyBuilder();
    IRBody ir = factory.build(ast);
    factory.dump( ast );
  }

  private IRBodyBuilder newBodyBuilder() {
    return new IRBodyBuilder(session.getTopLevelContext(), session.getGlobalEnvironment());
  }

  @Test(expected = NotCompilableException.class)
  public void sideEffects() {
    dump("1; y<-{sqrt(2);4}; launchMissile(3); 4");
  }
  
  @Test
  public void conditional() {
    dump("if(x < 0.5) y<- x*2 else y <- 3; y");
  }

  @Test
  public void conditionalAsExpression() {
    dump("if(z < 0.5) 1 else 2");
  }

  @Test
  public void conditionalAsExpressionWithNoElse() {
    dump("if(z < 0.5) 1");
  }
  
  @Test
  public void forLoop() {
    dump("x<-1:4; y<-0; for(n in x) { y <- y + n }");
  }

  @Test
  public void radford() {
    String forLoop = "for (i in 1:m) {\n" +
        "x = exp (tanh (a^2 * (b^2 + i/m)))\n" +
        "r[i%%10+1] = r[i%%10+1] + sum(x)\n" +
        "}";

    IRBody loopBody = build(forLoop);

    ControlFlowGraph cfg = new ControlFlowGraph(loopBody);
    DominanceTree dTree = new DominanceTree(cfg);
    SsaTransformer transformer = new SsaTransformer(cfg, dTree);
    transformer.transform();

    System.out.println(cfg);

    transformer.removePhiFunctions(new VariableMap(cfg));

    System.out.println("PHI REMOVED:");
    System.out.println(cfg);


//    BlockCompiler compiler = new BlockCompiler();
//    CompiledBody compiledBody = compiler.compile(cfg);
//
//    System.out.println(compiledBody.evaluate(session.getTopLevelContext(), session.getGlobalEnvironment()));

  }
  
  @Test
  @Ignore("wip")
  public void multipleAssigns() {
    evalIR("burt <- .Internal(rep.int(0, 29*29)) ; " +
    		   "dim(burt) <- c(29,29); " +
    		   "ii <- c(1,2);" +
    		   "jj <- c(3,4);" +
    		   "m <- c(134,33,2,46);" +
    		   "dim(m) <- c(2,2); " +
    		   "t <- function(x) .Internal(t.default(x)); " +
    		   "burt[jj, ii] <- t(   burt[ii, jj] <- m   );");
    
    assertThat(evalIR("burt[jj,ii]"), equalTo(c(134,2,33,46)));
    assertThat(evalIR("burt[ii,jj]"), equalTo(c(134,33,2,46)));
    

   
  }
  
  @Test
  public void interpretSimple() {
    assertThat(evalIR("x<-16; sqrt(x^2)"), equalTo(c(16)));
  }
  
  @Test
  public void complexAssignment() {
    assertThat(evalIR("x<-c(1:3); x[1] <- 99 "), equalTo(c(99)));
    assertThat(evalIR("x"), equalTo(c(99,2,3)));
    assertThat(evalIR("x<-list(1:3, 99, 1:4); x[[3]][5] <- 400; x[[3]] "), equalTo(c(1,2,3,4,400)));
  }
  
  @Test
  public void primitiveUsingSymbol() {
    assertThat(evalIR("x<-list(a=1,b=2); x$a"), equalTo(c(1)));
  }

  @Test
  public void interpretIf() {
    assertThat(evalIR("sqrt(4)"), equalTo(c(2)));
    assertThat(evalIR("if(sqrt(4) > 1) 'yes' else 'no'"), equalTo(c("yes")));
  }
  
  @Test
  public void interpretFor() {
    assertThat(evalIR("y<-1; for(i in 2:4) {y <- y * i }; y"), equalTo(c(24)));
  }

  @Test
  public void interpretForNext() {
    assertThat(evalIR("y<-0; for(i in 1:10) { if(i %% 2 == 0) { next };  y <- y + i }; y"),
        equalTo(c(25)));
  }
  
  @Test(expected = NotCompilableException.class)
  public void lazyArgument() {
    assertThat(evalIR("x <- quote(y)"), equalTo((SEXP)Symbol.get("y")));
  }
  
  @Test
  public void missingArgs() {
    evalIR("x <- 1:3");
    assertThat(evalIR("x[]"), equalTo(c_i(1,2,3)));
  }
  
  @Test
  public void returnFunction() {
    assertThat(evalIR("sqrt(4); return(2); 3; "), equalTo(c(2)));
  }
  
  @Test
  public void interpretRepeat() {
    assertThat(evalIR("y<-1; repeat {y <- y+1; if(y > 10) break }; y"), equalTo(c(11)));
  }
  
  @Test
  public void shortCircuitAnd() {
    assertThat(evalIR("FALSE && stop()"), equalTo(c(false)));
    assertThat(evalIR("1 && 2"), equalTo(c(true)));
    assertThat(evalIR("NA && 1"), equalTo(c(Logical.NA)));
    assertThat(evalIR("NA && FALSE"), equalTo(c(false)));
    assertThat(evalIR("42 && 1"), equalTo(c(true)));
    assertThat(evalIR("FALSE && stop(); NULL "), equalTo(NULL));
  
  }
  
  @Test
  public void shortCircuitOr() {
    assertThat(evalIR("TRUE || stop()"), equalTo(c(true)));
    assertThat(evalIR("0 || 2"), equalTo(c(true)));
    assertThat(evalIR("1 || 2"), equalTo(c(true)));
    assertThat(evalIR("1 || NA"), equalTo(c(true)));
    assertThat(evalIR("NA || 0"), equalTo(c(Logical.NA)));
    assertThat(evalIR("NA || 1"), equalTo(c(true)));
    assertThat(evalIR("0 || NA"), equalTo(c(Logical.NA)));
    assertThat(evalIR("1 || NA"), equalTo(c(true)));
    assertThat(evalIR("1 || stop(); NULL "), equalTo(NULL));
  }
 
  
  @Test
  public void assignmentInThunk() {
    assertThat(evalIR("length(x <- 42); x;"), equalTo(c(42)));    
  }
  
  private SEXP evalIR(String text) {
    System.out.println("======= " + text + "================");
    IRBody block = build(text);
    System.out.println(block.toString());    
    System.out.println();
    throw new AssumptionViolatedException("eval not implemented");
  }
  
  
  @Test
  public void closureBody() throws IOException {
    assumingBasePackagesLoad();
    topLevelContext.evaluate(
    RParser.parseSource(new InputStreamReader(getClass().getResourceAsStream("/meanOnline.R"))));
    
    Closure closure = (Closure) topLevelContext.getGlobalEnvironment().getVariable("mean.online");
    IRBodyBuilder factory = newBodyBuilder();
    factory.dump(closure.getBody());
  }
  
  private void dump(String rcode) {
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    IRBodyBuilder factory = newBodyBuilder();
    IRBody ir = factory.build(ast);
    
    System.out.println(ir.toString());
  }
  
  private IRBody build(String rcode) {
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    IRBodyBuilder factory = newBodyBuilder();
    return factory.build(ast);
  }
}
