package org.renjin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

public class ScriptEngineTest {


	@Test
	public void assureThatEngineCanBeLocatedAndInited() throws ScriptException {
		// create a script engine manager
		ScriptEngineManager factory = new ScriptEngineManager();
	
		// create an R engine
		ScriptEngine engine = factory.getEngineByName("Renjin");
		
		// evaluate JavaScript code from String
		engine.eval("print('Hello, World')");
	}

}

