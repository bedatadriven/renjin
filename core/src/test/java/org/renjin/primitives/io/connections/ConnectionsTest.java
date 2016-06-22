package org.renjin.primitives.io.connections;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.IntVector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


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
  public void readLinesSequence() {
    topLevelContext.getGlobalEnvironment().setVariable("conn", openResourceAsConn("lines.txt"));

    eval("line1 <- .Internal(readLines(conn, 1, TRUE, FALSE, 'UTF-8'))");
    eval("line2 <- .Internal(readLines(conn, 1, TRUE, FALSE, 'UTF-8'))");
    
    assertThat(eval("line1"), equalTo(c("This is the first line")));
    assertThat(eval("line2"), equalTo(c("And the second")));


  }

  @Test
  public void readTextGz() throws IOException {

    String path = getClass().getResource("/org/renjin/tobin.txt.gz").getFile();

    IntVector connHandle = Connections.file(topLevelContext, path, "rt", false, "UTF-8", false);

    assertThat(Connections.readChar(topLevelContext, connHandle, "durable".length(), false),
        equalTo("durable"));

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
  
  @Test
  public void testAppend() throws IOException {

    eval("tmp <- tempfile()");
    eval("c <- file(tmp, open='w')");
    eval("writeLines(con=c, text='Hello world') ");
    eval("close(c)");
    eval("c2 <- file(tmp, open='a')");
    eval("writeLines(con=c2, text='Hello again')");
    eval("close(c2)");
  
    File file = new File(getString("tmp"));
    assertTrue(file.exists());

    List<String> lines = Files.readLines(file, Charsets.UTF_8);
    
    assertThat(lines.size(), equalTo(2));
    assertThat(lines.get(0), equalTo("Hello world"));
    assertThat(lines.get(1), equalTo("Hello again"));
  }


  @Test
  public void testOverwrite() throws IOException {

    eval("tmp <- tempfile()");
    eval("c <- file(tmp, open='w')");
    eval("writeLines(con=c, text='Hello world') ");
    eval("close(c)");
    eval("c2 <- file(tmp, open='w')");
    eval("writeLines(con=c2, text='Hello again')");
    eval("close(c2)");

    File file = new File(getString("tmp"));
    assertTrue(file.exists());

    List<String> lines = Files.readLines(file, Charsets.UTF_8);

    assertThat(lines.size(), equalTo(1));
    assertThat(lines.get(0), equalTo("Hello again"));
  }
}
