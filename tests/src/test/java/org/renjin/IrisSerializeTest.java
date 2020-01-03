package org.renjin;

import org.junit.Test;
import org.renjin.repackaged.guava.io.Resources;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.serialization.HeadlessWriteContext;
import org.renjin.serialization.RDataReader;
import org.renjin.serialization.RDataWriter;
import org.renjin.serialization.Serialization;
import org.renjin.sexp.SEXP;

import javax.script.ScriptException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class IrisSerializeTest {

  /**
   * Ensure that the iris dataset serialized from R3.5.3 is identical to ours
   */
  @Test
  public void objectTest() throws IOException, ScriptException {

    URL resource = Resources.getResource("iris.rds");
    byte[] expectedOutput = Resources.toByteArray(resource);

    RDataReader reader = new RDataReader(new ByteArrayInputStream(expectedOutput));
    SEXP iris = reader.readFile();


    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.getScriptEngine();
    engine.put("iris2", iris);
    engine.eval("stopifnot(identical(iris, iris2))");

  }

  /**
   * Ensure that the iris dataset serializes byte-for-byte compared to
   * R 3.5.3
   */
  @Test
  public void test() throws IOException, ScriptException {


    URL resource = Resources.getResource("iris.rds");
    byte[] expectedOutput = Resources.toByteArray(resource);

    OutputStream checking = new OutputStream() {
      private int byteIndex = 0;

      @Override
      public void write(int b) throws IOException {
        byte expected = expectedOutput[byteIndex++];
        if(expected != (byte)b) {
          throw new AssertionError("mismatch");
        }
      }
    };


    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.getScriptEngine();
    engine.eval("str(iris)");
    SEXP iris = (SEXP) engine.eval("iris");

    RDataWriter writer = new RDataWriter(HeadlessWriteContext.INSTANCE, null, checking, Serialization.SerializationType.BINARY);
    writer.serialize(iris);
    writer.close();
  }

}
