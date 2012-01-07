package r.compiler.ir.tac;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import r.EvalTestCase;
import r.lang.Closure;
import r.lang.ExpressionVector;
import r.lang.SEXP;
import r.parser.RParser;

public class IRBuilderTest extends EvalTestCase {

  private IRFunctionTable functionTable = new IRFunctionTable();
  private IRScopeBuilder factory = new IRScopeBuilder(functionTable); 
  
  @Test
  public void simple() {
    ExpressionVector ast = RParser.parseSource("x + sqrt(x * y)\n");
    IRScope ir = factory.build(ast);
    
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
  public void interpretSimple() {
    assertThat(evalIR("x<-16; sqrt(x^2)"), equalTo(c(16)));
  }
  
  @Test
  public void complexAssignment() {
    assertThat(evalIR("x<-c(1:3); x[1] <- 99 "), equalTo(c(99)));
    assertThat(eval("x"), equalTo(c(99,2,3)));
    assertThat(evalIR("x<-list(1:3, 99, 1:4); x[[3]][5] <- 400; x[[3]] "), equalTo(c(1,2,3,4,400)));
  }
  
  @Test
  public void primitiveUsingSymbol() {
    assertThat(evalIR("x<-list(a=1,b=2); x$a"), equalTo(c(1)));
  }

  @Test
  public void interpretIf() {
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
  public void interpretRepeat() {
    assertThat(evalIR("y<-1; repeat {y <- y+1; if(y > 10) break }; y"), equalTo(c(11)));
  }
  
  private SEXP evalIR(String text) {
    System.out.println("======= " + text + "================");
    IRScope block = build(text);
    System.out.println(block.toString());    
    System.out.println();
    return block.evaluate(topLevelContext);
  }
  
  
  @Test
  public void closureBody() throws IOException {
    topLevelContext.init();
    topLevelContext.evaluate(
    RParser.parseSource(new InputStreamReader(getClass().getResourceAsStream("/meanOnline.R"))));
    
    Closure closure = (Closure) topLevelContext.getGlobalEnvironment().getVariable("mean.online");
    factory.dump(closure.getBody());
  }
  
  private void dump(String rcode) {
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    IRScope ir = factory.build(ast);
    
    System.out.println(ir.toString());
  }
  
  private IRScope build(String rcode) {
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    return factory.build(ast);
  }
}
