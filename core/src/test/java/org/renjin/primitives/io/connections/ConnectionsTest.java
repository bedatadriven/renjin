package org.renjin.primitives.io.connections;

import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class ConnectionsTest extends EvalTestCase {

  @Test
  public void readAllLines() {
    
    topLevelContext.getGlobalEnvironment().setVariable("conn", openResourceAsConn("lines.txt"));
    
    eval("lines <- .Internal(readLines(conn, -1, TRUE, FALSE, 'UTF-8'))");
 
    assertThat(eval("length(lines)"), equalTo(c_i(7)));
  }

  @Test
  public void readSomeLines() {
    
    topLevelContext.getGlobalEnvironment().setVariable("conn", openResourceAsConn("lines.txt"));
    
    eval("lines <- .Internal(readLines(conn, 2, TRUE, FALSE, 'UTF-8'))");
    assertThat(eval("length(lines)"), equalTo(c_i(2)));
  }
 
  
  @Test
  public void url() {
    assumingBasePackagesLoad();
    
    eval("x <- getCRANmirrors()");
    assertThat(eval("length(row.names(x)) > 10"), equalTo(c(true)));
    assertThat(eval("length(x$Name) > 10"), equalTo(c(true)));
    
  }
  
}
