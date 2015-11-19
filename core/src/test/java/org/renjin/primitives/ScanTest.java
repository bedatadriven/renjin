package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.StringVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class ScanTest extends EvalTestCase {

  @Test
  public void issue22() {
    
    global.setVariable("file.name", StringVector.valueOf( getClass().getResource("/scantest.txt").getFile()) );
    eval("x <- scan(file.name, nlines=200, comment.char='#')");
    eval("print(x)" );
    assertThat( eval("length(x)"), equalTo(c_i(585)));
    
  }

  @Test
  public void issueGitHub19()   {
    global.setVariable("file.name", StringVector.valueOf( getClass().getResource("/scantest.txt").getFile()) );
    eval("x <- scan(file.name, skip=3)");
    eval("print(x)" );
    assertThat( eval("length(x)"), equalTo(c_i(555)));
  }
  
  @Test
  public void whitespaceSplitter() {

    Scan.WhitespaceSplitter splitter = new Scan.WhitespaceSplitter("\"");
    
    assertThat(
        splitter.split("1.125256674 1.000000000 0.000000000"), 
              contains("1.125256674", "1.000000000", "0.000000000"));

    assertThat(
        splitter.split("  1.125256674 1.000000000   0.000000000  "),
        contains("1.125256674", "1.000000000", "0.000000000"));

  }

}
