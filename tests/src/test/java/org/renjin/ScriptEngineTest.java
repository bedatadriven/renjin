package org.renjin;

import org.junit.Before;
import org.junit.Test;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.StringVector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

public class ScriptEngineTest {

  private ScriptEngine engine;

  @Before
  public void setUp() {
    // create a script engine manager
    ScriptEngineManager factory = new ScriptEngineManager();

    engine = factory.getEngineByName("Renjin");
  }

  @Test
  public void assureThatEngineCanBeLocatedAndInited() throws ScriptException {


    // evaluate code from String
    engine.eval("print('Hello, World')");
  }

  @Test
  public void bquote() throws ScriptException {
    engine.eval("model <-  bquote(~0 + .(quote(births)))");
    engine.eval("expected <-  ~0 + births");

    LogicalVector result = (LogicalVector) engine.eval("model == expected");
    assertTrue(result.asLogical() == Logical.TRUE);
  }

  @Test
  public void userPackage() throws ScriptException {
    engine.eval("library('org.renjin.test.thirdparty')");
  }
  
  @Test
  public void redirectStreams() throws ScriptException {
    StringWriter outputWriter1 = new StringWriter();
    StringWriter errorWriter1 = new StringWriter();

    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("Renjin");

    scriptEngine.getContext().setWriter(outputWriter1);
    scriptEngine.getContext().setErrorWriter(errorWriter1);

    scriptEngine.eval("cat('Hello World')");
    scriptEngine.eval("cat('Goodbye cruel world!', file=stderr())");
    
    assertThat(outputWriter1.toString().trim(), equalTo("Hello World"));
    assertThat(errorWriter1.toString().trim(), equalTo("Goodbye cruel world!"));
    
    // Change the streams again
    StringWriter outputWriter2 = new StringWriter();
    StringWriter errorWriter2 = new StringWriter();
    scriptEngine.getContext().setWriter(outputWriter2);
    scriptEngine.getContext().setErrorWriter(errorWriter2);

    scriptEngine.eval("cat('Hello Redux')");
    scriptEngine.eval("cat('Goodbye again', file=stderr())");
    
    // Output should not be sent to original stream

    assertThat(outputWriter1.toString().trim(), equalTo("Hello World"));
    assertThat(errorWriter1.toString().trim(), equalTo("Goodbye cruel world!"));

    assertThat(outputWriter2.toString().trim(), equalTo("Hello Redux"));
    assertThat(errorWriter2.toString().trim(), equalTo("Goodbye again"));
  }
  
  @Test
  public void redirectInputStream() throws ScriptException {

    String input = "The night is dark and full of terrors";
    StringReader reader = new StringReader(input);

    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("Renjin");
    scriptEngine.getContext().setReader(reader);
    
    StringVector result = (StringVector) scriptEngine.eval("readLines(con=stdin(), n=1)");
    
    assertThat(result.getElementAsString(0), equalTo(input));
  }
}

