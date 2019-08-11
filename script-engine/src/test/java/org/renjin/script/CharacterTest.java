package org.renjin.script;

import org.junit.Test;
import org.renjin.parser.StringLiterals;
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

         engine.eval("print('åäö')");
         assertThat(outSw.toString(), equalTo("[1] \"åäö\"\n"));
      }
   }

   @Test
   public void testNonAsciiWithVar() throws ScriptException {
      RenjinScriptEngine engine = new RenjinScriptEngine();
      engine.eval("charVar <- 'åäö'");
      StringVector vec = (StringVector) engine.eval("charVar");
      assertThat(vec.asString(), equalTo("åäö"));
   }

   @Test
   public void testEscapeNonAsciiConfig() throws IOException, ScriptException {
      StringLiterals.ESCAPE_NON_ASCII = true;
      try (StringWriter outSw = new StringWriter();
           PrintWriter outputWriter = new PrintWriter(outSw)) {
         RenjinScriptEngine engine = new RenjinScriptEngine();
         engine.getSession().setStdOut(outputWriter);

         engine.eval("print('åäö')");
         assertThat(outSw.toString(), equalTo("[1] \"\\u00e5\\u00e4\\u00f6\"\n"));
         outSw.getBuffer().setLength(0);
         outSw.getBuffer().trimToSize();

         StringLiterals.ESCAPE_NON_ASCII = false;
         engine.eval("print('åäö')");
         assertThat(outSw.toString(), equalTo("[1] \"åäö\"\n"));
      }
   }
}
