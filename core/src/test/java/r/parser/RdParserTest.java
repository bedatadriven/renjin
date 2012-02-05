package r.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import r.EvalTestCase;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.StringVector;
import r.lang.Symbol;

public class RdParserTest extends EvalTestCase {

  @Test
  public void simple() throws IOException {
    
    RdParser parser = new RdParser();
    ListVector result = (ListVector) parser.R_ParseRd(new StringReader("\\name{plotf}"), Null.INSTANCE, false);
    assertThat(result.length(), equalTo(1));
 
    ListVector header = (ListVector) result.getElementAsSEXP(0);
    assertThat(header.getAttribute(Symbol.get("Rd_tag")), equalTo(c("\\name")));

    StringVector name = (StringVector) header.getElementAsSEXP(0);
    assertThat(name, equalTo(c("plotf")));
    //assertThat(name.getAttribute(Symbol.get("Rd_tag")), equalTo(c("VERB")));
    
  }
  
}
