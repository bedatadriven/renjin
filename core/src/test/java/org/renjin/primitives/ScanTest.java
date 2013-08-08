package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.StringVector;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ScanTest extends EvalTestCase {

  @Test
  public void issue22() {
    
    global.setVariable("file.name", StringVector.valueOf( getClass().getResource("/scantest.txt").getFile()) );
    eval("x <- scan(file.name, nlines=200, comment.char='#')");
    eval("print(x)" );
    assertThat( eval("length(x)"), equalTo(c_i(585)));
    
  }

  @Test
  public void issueGitHub19() {
    global.setVariable("file.name", StringVector.valueOf( getClass().getResource("/scantest.txt").getFile()) );
    eval("x <- scan(file.name, skip=3)");
    eval("print(x)" );
    assertThat( eval("length(x)"), equalTo(c_i(555)));
  }
}
