/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
