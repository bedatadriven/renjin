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
import java.io.Writer;

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
    StringWriter stringWriter = new StringWriter();

    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("Renjin");

    Writer originalWriter = scriptEngine.getContext().getWriter();
    Writer errorWriter = scriptEngine.getContext().getErrorWriter();

    try {

      scriptEngine.getContext().setWriter(stringWriter);
      scriptEngine.getContext().setErrorWriter(stringWriter);

      scriptEngine.eval("cat('Hello World')");
      scriptEngine.eval("cat('Goodbye cruel world!', file=stderr())");

    } finally {

      scriptEngine.getContext().setWriter(originalWriter);
      scriptEngine.getContext().setErrorWriter(errorWriter);
    }

    assertThat(stringWriter.toString().trim(), equalTo("Hello World"));
    assertThat(errorWriter.toString().trim(), equalTo("Goodbye cruel world!"));
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

