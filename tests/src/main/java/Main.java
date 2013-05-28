import javax.script.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

/**
 * Main class which executes a single test, useful for debugging
 * individual tests in Java IDEs
 */
public class Main {

  public static void main(String[] args) throws FileNotFoundException, ScriptException, NoSuchMethodException {
    String file = args[0];

    // create a script engine manager
    ScriptEngineManager factory = new ScriptEngineManager();

    // create an R engine
    ScriptEngine engine = factory.getEngineByName("Renjin");
    engine.eval(new FileReader("src/test/R/" + file));

    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
    for(String name : bindings.keySet()) {
      if(name.startsWith("test.")) {
        Invocable invocable = (Invocable) engine;
        invocable.invokeFunction(name);
      }
    }
  }
}
