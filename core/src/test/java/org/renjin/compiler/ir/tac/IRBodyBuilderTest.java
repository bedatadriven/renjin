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
package org.renjin.compiler.ir.tac;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.renjin.EvalTestCase;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.UseDefMap;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertThat;


public class IRBodyBuilderTest extends EvalTestCase {

  private Session session;

  public static void stop() {
    throw new EvalException("stop!");
  }

  @Before
  public void setUpSession() {
    session = new SessionBuilder().build();
  }

  @Test
  public void simple() {
    ExpressionVector ast = RParser.parseSource("x + sqrt(x * y)\n");
    IRBodyBuilder factory = newBodyBuilder();
    IRBody ir = factory.build(ast);
    factory.dump( ast );
  }

  private IRBodyBuilder newBodyBuilder() {
    return new IRBodyBuilder(new RuntimeState(session.getTopLevelContext(), session.getGlobalEnvironment()));
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
    SsaTransformer transformer = new SsaTransformer(cfg);
    transformer.transform();

    System.out.println(cfg);
    UseDefMap useDefMap = new UseDefMap(cfg);
    TypeSolver types = new TypeSolver(cfg, useDefMap);
    
    transformer.removePhiFunctions(types);

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
    
    assertThat(evalIR("burt[jj,ii]"), elementsIdenticalTo(c(134,2,33,46)));
    assertThat(evalIR("burt[ii,jj]"), elementsIdenticalTo(c(134,33,2,46)));
  }
  
  @Test
  public void interpretSimple() {
    assertThat(evalIR("x<-16; sqrt(x^2)"), elementsIdenticalTo(c(16)));
  }
  
  @Test
  public void complexAssignment() {
    assertThat(evalIR("x<-c(1:3); x[1] <- 99 "), elementsIdenticalTo(c(99)));
    assertThat(evalIR("x"), elementsIdenticalTo(c(99,2,3)));
    assertThat(evalIR("x<-list(1:3, 99, 1:4); x[[3]][5] <- 400; x[[3]] "), elementsIdenticalTo(c(1,2,3,4,400)));
  }
  
  @Test
  public void primitiveUsingSymbol() {
    assertThat(evalIR("x<-list(a=1,b=2); x$a"), elementsIdenticalTo(c(1)));
  }

  @Test
  public void interpretIf() {
    assertThat(evalIR("sqrt(4)"), elementsIdenticalTo(c(2)));
    assertThat(evalIR("if(sqrt(4) > 1) 'yes' else 'no'"), elementsIdenticalTo(c("yes")));
  }
  
  @Test
  public void interpretFor() {
    assertThat(evalIR("y<-1; for(i in 2:4) {y <- y * i }; y"), elementsIdenticalTo(c(24)));
  }

  @Test
  public void interpretForNext() {
    assertThat(evalIR("y<-0; for(i in 1:10) { if(i %% 2 == 0) { next };  y <- y + i }; y"),
        elementsIdenticalTo(c(25)));
  }
  
  @Test(expected = NotCompilableException.class)
  public void lazyArgument() {
    assertThat(evalIR("x <- quote(y)"), identicalTo((SEXP)Symbol.get("y")));
  }
  
  @Test
  public void missingArgs() {
    evalIR("x <- 1:3");
    assertThat(evalIR("x[]"), elementsIdenticalTo(c_i(1,2,3)));
  }
  
  @Test
  public void returnFunction() {
    assertThat(evalIR("sqrt(4); return(2); 3; "), elementsIdenticalTo(c(2)));
  }
  
  @Test
  public void interpretRepeat() {
    assertThat(evalIR("y<-1; repeat {y <- y+1; if(y > 10) break }; y"), elementsIdenticalTo(c(11)));
  }

  @Test
  public void complexAssignmentClosureSetter() {
    IRBody block = build("storage.mode(x) <- 'integer'");
    System.out.println(block);
  }
  
  @Test
  public void shortCircuitAnd() {
    assertThat(evalIR("FALSE && stop()"), elementsIdenticalTo(c(false)));
    assertThat(evalIR("1 && 2"), elementsIdenticalTo(c(true)));
    assertThat(evalIR("NA && 1"), elementsIdenticalTo(c(Logical.NA)));
    assertThat(evalIR("NA && FALSE"), elementsIdenticalTo(c(false)));
    assertThat(evalIR("42 && 1"), elementsIdenticalTo(c(true)));
    assertThat(evalIR("FALSE && stop(); NULL "), identicalTo(NULL));
  
  }
  
  @Test
  public void shortCircuitOr() {
    assertThat(evalIR("TRUE || stop()"), elementsIdenticalTo(c(true)));
    assertThat(evalIR("0 || 2"), elementsIdenticalTo(c(true)));
    assertThat(evalIR("1 || 2"), elementsIdenticalTo(c(true)));
    assertThat(evalIR("1 || NA"), elementsIdenticalTo(c(true)));
    assertThat(evalIR("NA || 0"), elementsIdenticalTo(c(Logical.NA)));
    assertThat(evalIR("NA || 1"), elementsIdenticalTo(c(true)));
    assertThat(evalIR("0 || NA"), elementsIdenticalTo(c(Logical.NA)));
    assertThat(evalIR("1 || NA"), elementsIdenticalTo(c(true)));
    assertThat(evalIR("1 || stop(); NULL "), identicalTo(NULL));
  }

  @Test
  public void scale() {
    Closure closure = (Closure) topLevelContext.evaluate(Symbol.get("scale.default"));

    IRBodyBuilder factory = newBodyBuilder();
    IRBody ir = factory.build(closure.getBody());

    System.out.println(ir.toString());
  }
  

  @Test
  public void assignmentInThunk() {
    assertThat(evalIR("length(x <- 42); x;"), elementsIdenticalTo(c(42)));
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

    topLevelContext.evaluate(
        RParser.parseSource(new InputStreamReader(getClass().getResourceAsStream("/meanOnline.R"))));
    
    Closure closure = (Closure) topLevelContext.getGlobalEnvironment().getVariable(topLevelContext, "mean.online");
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
