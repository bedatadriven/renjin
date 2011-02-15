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

package r.lang;

import org.junit.Before;
import org.junit.Test;
import r.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class S3Test extends EvalTestCase {

  @Before
  public void declareWrappers() {
    eval( "inherits <- function(x, what, which=FALSE) { .Internal(inherits(x,what,which)) } ");
  }

  @Test
  public void implicitClasses() {

    eval(  "x <- 10" );
    assertThat( eval( "class(x)" ), equalTo( c( "numeric" )));
    assertThat( eval( "oldClass(x) "), equalTo( NULL ));
    assertThat( eval( "inherits(x, \"a\") "), equalTo( c(false) ));
  }


  @Test
  public void declaredClasses() {

    eval( "x<-1 ");
    eval( "class(x) <- c(\"a\", \"b\") ");
    assertThat( eval( "inherits(x, \"a\")"), equalTo( c(true) ));
    assertThat( eval( "inherits(x, \"a\", TRUE)"), equalTo( c_i(1) ));
    assertThat( eval( "inherits(x, c(\"a\", \"b\", \"c\"), TRUE) "), equalTo( c_i(1, 2, 0) ));

  }

}
