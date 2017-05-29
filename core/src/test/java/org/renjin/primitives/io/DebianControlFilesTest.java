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
package org.renjin.primitives.io;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

import java.io.IOException;

import static org.junit.Assert.assertThat;


public class DebianControlFilesTest extends EvalTestCase {

  @Test
  public void licenseTest() throws IOException {
   
    
    SEXP conn = openResourceAsConn("/org/renjin/share/licenses/license.db");
    topLevelContext.getEnvironment().setVariable(topLevelContext, "x",
        DebianControlFiles.readDCF(topLevelContext, conn, Null.INSTANCE, true));
    
    assertThat(eval("dim(x)"), elementsIdenticalTo(c_i(29,7)));
    assertThat(eval("dimnames(x)[[1]]"), identicalTo(NULL));
    assertThat(eval("dimnames(x)[[2]]"), elementsIdenticalTo(c("Name","Abbrev", "Version", "SSS", "OSI", "FSF", "URL")));
    assertThat(eval("x[13,'Abbrev']"), elementsIdenticalTo(c("FreeBSD")));
    assertThat(eval("x[13,'Abbrev']"), elementsIdenticalTo(c("FreeBSD")));
    assertThat(eval("is.na(x[14,'FSF'])"), elementsIdenticalTo(c(true)));

  }


  @Test
  public void continuationTest() throws IOException {
   
    
    SEXP conn = openResourceAsConn("continuation.dcf");
    SEXP db = DebianControlFiles.readDCF(topLevelContext, conn, Null.INSTANCE, true);
       
    topLevelContext.getEnvironment().setVariable(topLevelContext, "x",
        db);

    
    assertThat(eval("x[1,'Description']"), elementsIdenticalTo(
        c("Formally defined methods and classes for R objects, " +
        		"plus other programming tools, as described in the reference")));

  }

  
}

