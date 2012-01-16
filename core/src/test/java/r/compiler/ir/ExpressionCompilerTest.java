package r.compiler.ir;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import r.compiler.CompiledBody;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.ExpressionVector;
import r.lang.IntVector;
import r.lang.SEXP;
import r.lang.Symbol;
import r.parser.RParser;

public class ExpressionCompilerTest {
  
  private Context context;

  @Before
  public void setUp() {
    context = Context.newTopLevelContext();

  }
  
  @Test
  public void simplestTest() throws Exception {
    
    context.getEnvironment().setVariable(Symbol.get("x"), new DoubleVector(1,2,3,4));
    
    DoubleVector result = (DoubleVector) compileAndEval(context, "x<-4; x\n");
   
    assertThat(result.getElementAsDouble(0), equalTo(4d));
  }
  
  @Test
  public void ifStatement() throws Exception {
        
    DoubleVector result = (DoubleVector) compileAndEval(context, "if(TRUE) 42 else 5\n");
   
    assertThat(result.getElementAsDouble(0), equalTo(42d));
    
  }
  
  @Test
  public void dynamicCall() throws Exception {
    IntVector result = (IntVector) compileAndEval(context, "x<-5; length(x)\n");
    assertThat(result.getElementAsInt(0), equalTo(1));

  }
  
  @Test
  public void primitiveCall() throws Exception {
    
    DoubleVector result = (DoubleVector) compileAndEval(context, "1 + 1\n");
    assertThat(result.getElementAsInt(0), equalTo(2));

  }
  
  @Test
  @Ignore
  public void forLoop() throws Exception {
    DoubleVector result = (DoubleVector) compileAndEval(context, "x <- 0; for(i in 1:10) x <- x + 10; \n");
    assertThat(result.getElementAsInt(0), equalTo(2));
    
  }


  private SEXP compileAndEval(Context context, String code)
      throws InstantiationException, IllegalAccessException {
    ExpressionVector exp = RParser.parseSource(code);
    Class<CompiledBody> compiled = ExpressionCompiler.compile(exp);
    
    return compiled.newInstance().eval(context, context.getEnvironment());
  }

}
