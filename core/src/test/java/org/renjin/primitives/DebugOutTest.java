/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives;

import com.google.common.base.Charsets;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.CHARSEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DebugOutTest extends EvalTestCase {

  @Test
  public void simpleDebugStderr() {
      assertThat( eval( "debug.stderr(\"1234\")"), equalTo( c("1234")  )) ;
  }

  @Test
  public void testDebugFilename() {
     if (true) {
      // TODO: disable when keepSrcRef is not set in parse options.
      
      
      assertThat( eval( "{ debug.filename() }" ).asString(), equalTo( eval("\"inline-source\"").asString()  ) ) ;
     }
  }

  @Test
  public void testDebugLineno() {
     if (true) {
      // TODO: disable when keepSrcRef is not set in parse options.
      assertThat( eval( "{ debug.lineno() }" ).asReal(), equalTo( eval("1").asReal() ) ) ;
     }
  }

}
