package org.renjin.primitives.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import java.io.IOException;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.primitives.Print;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.ResourceConnection;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;


public class DebianControlFilesTest extends EvalTestCase {

  @Test
  public void licenseTest() throws IOException {
   
    
    ResourceConnection conn = new ResourceConnection(getClass(), "/org/renjin/share/licenses/license.db");
    topLevelContext.getEnvironment().setVariable("x",
        DebianControlFiles.readDCF(conn, Null.INSTANCE, true));
    
    assertThat(eval("dim(x)"), equalTo(c_i(29,7)));
    assertThat(eval("dimnames(x)[[1]]"), equalTo(NULL));
    assertThat(eval("dimnames(x)[[2]]"), equalTo(c("Name","Abbrev", "Version", "SSS", "OSI", "FSF", "URL")));
    assertThat(eval("x[13,'Abbrev']"), equalTo(c("FreeBSD")));  
    assertThat(eval("x[13,'Abbrev']"), equalTo(c("FreeBSD")));  
    assertThat(eval("is.na(x[14,'FSF'])"), equalTo(c(true)));  

  }


  @Test
  public void continuationTest() throws IOException {
   
    
    ResourceConnection conn = new ResourceConnection(getClass(), "continuation.dcf");
    SEXP db = DebianControlFiles.readDCF(conn, Null.INSTANCE, true);
       
    topLevelContext.getEnvironment().setVariable("x",
        db);

    
    assertThat(eval("x[1,'Description']"), equalTo(
        c("Formally defined methods and classes for R objects, " +
        		"plus other programming tools, as described in the reference")));

  }

  
}

