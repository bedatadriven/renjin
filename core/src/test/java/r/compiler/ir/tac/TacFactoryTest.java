package r.compiler.ir.tac;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import r.EvalTestCase;
import r.lang.Closure;
import r.lang.ExpressionVector;
import r.lang.SEXP;
import r.parser.RParser;

public class TacFactoryTest extends EvalTestCase {

  private TacFactory factory = new TacFactory(); 
  
  @Test
  public void simple() {
    ExpressionVector ast = RParser.parseSource("x <- 4; x + 3 * sqrt(x)\n");
    List<Node> ir = factory.build(ast);
    
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
  public void closureBody() throws IOException {
    topLevelContext.init();
    RParser.parseSource(new InputStreamReader(getClass().getResourceAsStream("/meanOnline.R")))
    .evaluate(topLevelContext, topLevelContext.getEnvironment());
   
    Closure closure = (Closure) topLevelContext.getGlobalEnvironment().getVariable("mean.online");
    factory.dump(closure.getBody());
  }
  
  private void dump(String rcode) {
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    List<Node> ir = factory.build(ast);
    
    factory.dump( ast );
  }
  
  
}
