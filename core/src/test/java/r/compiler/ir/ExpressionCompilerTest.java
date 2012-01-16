package r.compiler.ir;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import r.compiler.CompiledBody;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.ExpressionVector;
import r.lang.IntVector;
import r.lang.Symbol;
import r.parser.RParser;

public class ExpressionCompilerTest {
  
  @Test
  public void simplestTest() throws Exception {
    
    
    Context context = Context.newTopLevelContext();
    context.getEnvironment().setVariable(Symbol.get("x"), new DoubleVector(1,2,3,4));
    
    ExpressionVector exp = RParser.parseSource("x<-4; x\n");
    Class<CompiledBody> compiled = ExpressionCompiler.compile(exp);
    
    DoubleVector result = (DoubleVector) compiled.newInstance().eval(context, context.getEnvironment());
   
    assertThat(result.getElementAsDouble(0), equalTo(4d));
  }

}
