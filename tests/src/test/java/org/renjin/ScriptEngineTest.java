package org.renjin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;

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
	  Assert.assertTrue(result.asLogical() == Logical.TRUE);
	}

  @Test
  public void userPackage() throws ScriptException {
    engine.eval("library('org.renjin.test.thirdparty')");
  }
}

