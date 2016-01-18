package org.renjin.compiler.ir.tac;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.parser.RParser;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class IRBodyBuilderTest extends EvalTestCase {

  private IRFunctionTable functionTable = new IRFunctionTable();
  
  @Test
  public void simple() {
    ExpressionVector ast = RParser.parseInlineSource("x + sqrt(x * y)\n");
    IRBodyBuilder factory = new IRBodyBuilder(functionTable); 
    IRBody ir = factory.build(ast);
    factory.dump( ast );
  }

  @Test
  public void sideEffects() {
    dump("1; y<-{sqrt(2);4}; launchMissile(3); 4");
  }
  
  @Test
  public void conditional() {
    dump("x <- rand(); if(x < 0.5) y<- x*2 else y <- 3; y");
  }

  @Test
  public void conditionalAsExpression() {
    dump("if(rand() < 0.5) 1 else 2");
  }

  @Test
  public void conditionalAsExpressionWithNoElse() {
    dump("if(rand() < 0.5) 1");
  }
  
  @Test
  public void forLoop() {
    dump("x<-1:4; y<-0; for(n in x) { y <- y + n }");
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
  
  @Ignore
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
  
  @Test
  public void lazyArgument() {
    assertThat(evalIR("x <- quote(y)"), equalTo((SEXP)Symbol.get("y")));
  }
  
  @Test
  public void primitivesWithElipses() {
    assertThat(evalIR("x<-10:20; f<-function(...) x[...]; f(1);"), equalTo(c_i(10)));
  }
  
  
  @Ignore
  @Test
  public void complexFunctionValue() {
    assertThat(evalIR("x<-list(f=function() { 42 }); x$f();"), equalTo(c(42)));
  }
  
  @Test
  public void missingArgs() {
    evalIR("x <- 1:3");
    assertThat(evalIR("x[]"), equalTo(c_i(1,2,3)));
  }
  
  @Test
  public void returnFunction() {
    assertThat(evalIR("f<-function() { sqrt(4); return(2); 3}; f(); "), equalTo(c(2)));
  }
  
  @Test
  public void interpretRepeat() {
    assertThat(evalIR("y<-1; repeat {y <- y+1; if(y > 10) break }; y"), equalTo(c(11)));
  }
  
  @Test
  public void shortCircuitAnd() {
    assertThat(evalIR("FALSE && explode()"), equalTo(c(false)));
    assertThat(evalIR("1 && 2"), equalTo(c(true)));
    assertThat(evalIR("NA && 1"), equalTo(c(Logical.NA)));
    assertThat(evalIR("NA && FALSE"), equalTo(c(false)));
    assertThat(evalIR("42 && 1"), equalTo(c(true)));
    assertThat(evalIR("FALSE && rocket(); NULL "), equalTo(NULL));
  
  }
  
  @Test
  public void shortCircuitOr() {
    assertThat(evalIR("TRUE || explode()"), equalTo(c(true)));
    assertThat(evalIR("0 || 2"), equalTo(c(true)));
    assertThat(evalIR("1 || 2"), equalTo(c(true)));
    assertThat(evalIR("1 || NA"), equalTo(c(true)));
    assertThat(evalIR("NA || 0"), equalTo(c(Logical.NA)));
    assertThat(evalIR("NA || 1"), equalTo(c(true)));
    assertThat(evalIR("0 || NA"), equalTo(c(Logical.NA)));
    assertThat(evalIR("1 || NA"), equalTo(c(true)));
    assertThat(evalIR("1 || rocket(); NULL "), equalTo(NULL));
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
    return block.evaluate(topLevelContext);
  }
  
  
  @Test
  public void closureBody() throws IOException {
    assumingBasePackagesLoad();
    topLevelContext.evaluate(
      RParser.parseSource(new InputStreamReader(getClass().getResourceAsStream("/meanOnline.R")),
                          new CHARSEXP("/meanOnline.R")
                         )
    );
    
    Closure closure = (Closure) topLevelContext.getGlobalEnvironment().getVariable("mean.online");
    IRBodyBuilder factory = new IRBodyBuilder(functionTable);
    factory.dump(closure.getBody());
  }
  
  private void dump(String rcode) {
    ExpressionVector ast = RParser.parseInlineSource(rcode + "\n");
    IRBodyBuilder factory = new IRBodyBuilder(functionTable);
    IRBody ir = factory.build(ast);
    
    System.out.println(ir.toString());
  }
  
  private IRBody build(String rcode) {
    ExpressionVector ast = RParser.parseInlineSource(rcode + "\n");
    IRBodyBuilder factory = new IRBodyBuilder(functionTable);
    return factory.build(ast);
  }
}
