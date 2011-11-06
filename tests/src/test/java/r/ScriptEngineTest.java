package r;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Ignore;
import org.junit.Test;

public class ScriptEngineTest {


	@Ignore("not yet working")
	@Test
	public void assureThatEngineCanBeLocatedAndInited() throws ScriptException {
		// create a script engine manager
		ScriptEngineManager factory = new ScriptEngineManager();
		// create a JavaScript engine
		ScriptEngine engine = factory.getEngineByName("Renjin");
		// evaluate JavaScript code from String
		engine.eval("print('Hello, World')");
	}

}

