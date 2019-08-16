package org.renjin.script;

import org.junit.Test;
import org.renjin.sexp.StringVector;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CharacterTest {

   @Test
   public void testStdOut() throws IOException, ScriptException {
      try (StringWriter outSw = new StringWriter();
           PrintWriter outputWriter = new PrintWriter(outSw)) {
         RenjinScriptEngine engine = new RenjinScriptEngine();
         engine.getSession().setStdOut(outputWriter);

         engine.eval("print('Hello World')");
         assertThat(outSw.toString(), equalTo("[1] \"Hello World\"\n"));
         outSw.getBuffer().setLength(0);
         outSw.getBuffer().trimToSize();

         engine.eval("cat('Hello\nWorld')");
         assertThat(outSw.toString(), equalTo("Hello\nWorld"));
         outSw.getBuffer().setLength(0);
         outSw.getBuffer().trimToSize();

         engine.eval("print('åäö')");
         assertThat(outSw.toString(), equalTo("[1] \"åäö\"\n"));
         outSw.getBuffer().setLength(0);
         outSw.getBuffer().trimToSize();

         engine.eval("cat('åäö')");
         assertThat(outSw.toString(), equalTo("åäö"));

         outSw.getBuffer().setLength(0);
         outSw.getBuffer().trimToSize();

         engine.eval("a <- '\u00b5';print(a)");
         assertThat(outSw.toString(), equalTo("[1] \"µ\"\n"));
      }
   }

   @Test
   public void testNonAsciiWithVar() throws ScriptException {
      RenjinScriptEngine engine = new RenjinScriptEngine();
      engine.eval("charVar <- 'åäö'");
      StringVector vec = (StringVector) engine.eval("charVar");
      assertThat(vec.asString(), equalTo("åäö"));
   }

}
