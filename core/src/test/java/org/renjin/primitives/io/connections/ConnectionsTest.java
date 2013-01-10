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
  public void summary() {
    eval("x <- .Internal(summary.connection(.Internal(stdin())))");
    assertThat(eval("x$description"), equalTo(c("stdin")));
    assertThat(eval("x$class"), equalTo(c("terminal")));
    assertThat(eval("x$mode"), equalTo(c("r")));
    assertThat(eval("x$text"), equalTo(c("text")));
    assertThat(eval("x$opened"), equalTo(c("opened")));
    assertThat(eval("x$`can read`"), equalTo(c("yes")));
    assertThat(eval("x$`can write`"), equalTo(c("no")));

    eval("y <- .Internal(summary.connection(.Internal(file('target/testwb', 'wb', TRUE, 'UTF8', FALSE))))");
    //assertThat(eval("y$description"), equalTo(c("/dev/null")));
    assertThat(eval("y$class"), equalTo(c("file")));
    assertThat(eval("y$mode"), equalTo(c("wb")));
    assertThat(eval("y$text"), equalTo(c("binary")));
    assertThat(eval("y$opened"), equalTo(c("opened")));
    assertThat(eval("y$`can read`"), equalTo(c("no")));
    assertThat(eval("y$`can write`"), equalTo(c("yes")));
    
    // When the openSpec is left blank, potentially both read and write 
    eval("z <- .Internal(summary.connection(.Internal(file('target/testwb', '', TRUE, 'UTF8', FALSE))))");
    assertThat(eval("z$mode"), equalTo(c("r")));
    assertThat(eval("z$text"), equalTo(c("text")));
    assertThat(eval("z$opened"), equalTo(c("closed")));
    assertThat(eval("z$`can read`"), equalTo(c("yes")));
    assertThat(eval("z$`can write`"), equalTo(c("yes")));
    
    // however if 'r' is explicitly provided, then no write
    eval("z <- .Internal(summary.connection(.Internal(file('target/testwb', 'r', TRUE, 'UTF8', FALSE))))");
    assertThat(eval("z$mode"), equalTo(c("r")));
    assertThat(eval("z$text"), equalTo(c("text")));
    assertThat(eval("z$opened"), equalTo(c("opened")));
    assertThat(eval("z$`can read`"), equalTo(c("yes")));
    assertThat(eval("z$`can write`"), equalTo(c("no")));
   
  }
  
}
