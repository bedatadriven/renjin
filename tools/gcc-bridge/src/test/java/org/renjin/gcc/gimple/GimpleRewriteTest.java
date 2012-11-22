package org.renjin.gcc.gimple;


import org.junit.Test;
import org.renjin.gcc.CallingConvention;
import org.renjin.gcc.gimple.rewrite.RefParamRemover;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class GimpleRewriteTest {

  @Test
  public void testDerefence() throws IOException {

    GimpleParser parser = new GimpleParser(new CallingConvention());
    List<GimpleFunction> functions = parser.parse(new InputStreamReader(getClass().getResourceAsStream("ioffst.gimple")));

    RefParamRemover remover = new RefParamRemover();
    remover.apply(functions.get(0));

  }

}
