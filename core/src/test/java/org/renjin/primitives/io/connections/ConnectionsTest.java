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
package org.renjin.primitives.io.connections;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.io.Files;
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
    
    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "conn", openResourceAsConn("lines.txt"));
    
    eval("lines <- .Internal(readLines(conn, -1, TRUE, FALSE, 'UTF-8'))");
 
    assertThat(eval("length(lines)"), elementsIdenticalTo(c_i(7)));
  }

  @Test
  public void readSomeLines() {
    
    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "conn", openResourceAsConn("lines.txt"));
    
    eval("lines <- .Internal(readLines(conn, 2, TRUE, FALSE, 'UTF-8'))");
    assertThat(eval("length(lines)"), elementsIdenticalTo(c_i(2)));
  }
  
  @Test
  public void readLinesSequence() {
    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "conn", openResourceAsConn("lines.txt"));

    eval("line1 <- .Internal(readLines(conn, 1, TRUE, FALSE, 'UTF-8'))");
    eval("line2 <- .Internal(readLines(conn, 1, TRUE, FALSE, 'UTF-8'))");
    
    assertThat(eval("line1"), elementsIdenticalTo(c("This is the first line")));
    assertThat(eval("line2"), elementsIdenticalTo(c("And the second")));


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
    assertThat(eval("x$description"), elementsIdenticalTo(c("stdin")));
    assertThat(eval("x$class"), elementsIdenticalTo(c("terminal")));
    assertThat(eval("x$mode"), elementsIdenticalTo(c("r")));
    assertThat(eval("x$text"), elementsIdenticalTo(c("text")));
    assertThat(eval("x$opened"), elementsIdenticalTo(c("opened")));
    assertThat(eval("x$`can read`"), elementsIdenticalTo(c("yes")));
    assertThat(eval("x$`can write`"), elementsIdenticalTo(c("no")));

    eval("y <- .Internal(summary.connection(.Internal(file('target/testwb', 'wb', TRUE, 'UTF8', FALSE))))");
    //assertThat(eval("y$description"), equalTo(c("/dev/null")));
    assertThat(eval("y$class"), elementsIdenticalTo(c("file")));
    assertThat(eval("y$mode"), elementsIdenticalTo(c("wb")));
    assertThat(eval("y$text"), elementsIdenticalTo(c("binary")));
    assertThat(eval("y$opened"), elementsIdenticalTo(c("opened")));
    assertThat(eval("y$`can read`"), elementsIdenticalTo(c("no")));
    assertThat(eval("y$`can write`"), elementsIdenticalTo(c("yes")));
    
    // When the openSpec is left blank, potentially both read and write 
    eval("z <- .Internal(summary.connection(.Internal(file('target/testwb', '', TRUE, 'UTF8', FALSE))))");
    assertThat(eval("z$mode"), elementsIdenticalTo(c("r")));
    assertThat(eval("z$text"), elementsIdenticalTo(c("text")));
    assertThat(eval("z$opened"), elementsIdenticalTo(c("closed")));
    assertThat(eval("z$`can read`"), elementsIdenticalTo(c("yes")));
    assertThat(eval("z$`can write`"), elementsIdenticalTo(c("yes")));
    
    // however if 'r' is explicitly provided, then no write
    eval("z <- .Internal(summary.connection(.Internal(file('target/testwb', 'r', TRUE, 'UTF8', FALSE))))");
    assertThat(eval("z$mode"), elementsIdenticalTo(c("r")));
    assertThat(eval("z$text"), elementsIdenticalTo(c("text")));
    assertThat(eval("z$opened"), elementsIdenticalTo(c("opened")));
    assertThat(eval("z$`can read`"), elementsIdenticalTo(c("yes")));
    assertThat(eval("z$`can write`"), elementsIdenticalTo(c("no")));
   
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
