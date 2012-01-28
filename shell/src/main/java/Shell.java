import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 * Class is meant to be a general purpose shell.
 * @author jamie
 *
 */
public class Shell {

  public static void main(String args[]) throws IOException, ScriptException{
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("Renjin");   
    assert engine !=null;
    while(true){
      System.out.print("> ");
      assert reader!=null;
      System.out.println(engine.eval(reader.readLine()));
    }
    
    
  }
  
}
