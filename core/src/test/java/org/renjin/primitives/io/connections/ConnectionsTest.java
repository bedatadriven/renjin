package org.renjin.primitives.io.connections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.renjin.EvalTestCase;


public class ConnectionsTest extends EvalTestCase {

  @Test
  public void readAllLines() {
    
    ResourceConnection conn = new ResourceConnection(getClass(), "lines.txt");
    topLevelContext.getEnvironment().setVariable("conn", Connections.asSexp(conn));
    
    eval("lines <- .Internal(readLines(conn, -1, TRUE, FALSE, 'UTF-8'))");
 
    assertThat(eval("length(lines)"), equalTo(c_i(7)));
  }
 
  @Test
  public void readSomeLines() {
    
    ResourceConnection conn = new ResourceConnection(getClass(), "lines.txt");
    topLevelContext.getEnvironment().setVariable("conn", Connections.asSexp(conn));
    
    eval("lines <- .Internal(readLines(conn, 2, TRUE, FALSE, 'UTF-8'))");
    assertThat(eval("length(lines)"), equalTo(c_i(2)));
  }

  @Test
  public void fifo() {
    
    
  }
}
