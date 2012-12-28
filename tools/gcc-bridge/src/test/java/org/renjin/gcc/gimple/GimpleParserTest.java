package org.renjin.gcc.gimple;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.gcc.CallingConvention;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

public class GimpleParserTest {

  private GimpleParser parser;

  @Before
  public void setUp() throws Exception {
    parser = new GimpleParser(new CallingConvention());
  }

  @Ignore
	@Test
	public void swilk() throws IOException {

    List<GimpleFunction> functions = parse("swilk.c.143t.optimized");
	}



  @Test
  public void gccVersion4_4_6() throws IOException {
    List<GimpleFunction> functions = parse("gimple4.4.6");

		for(GimpleFunction fn : functions) {
		}
  }
  
  @Test
  @Ignore("wip")
  public void pointerPrecedence() throws IOException {
    GimpleParser parser = new GimpleParser(new CallingConvention());
    GimpleExpr expr = parser.parseExpr("&(*x(D))[y]");
    
  }

  private List<GimpleFunction> parse(String resourceName) throws IOException {
    InputStream in = getClass().getResourceAsStream(resourceName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    return parser.parse(reader);
  }
	
}
