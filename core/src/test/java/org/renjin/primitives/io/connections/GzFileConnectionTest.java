/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.SEXP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GzFileConnectionTest extends EvalTestCase {

  @Test
  public void readCompressed() throws IOException {

    FileObject file = VFS.getManager().resolveFile(getClass().getResource("test.txt.gz").getFile());
    GzFileConnection conn = new GzFileConnection(file);

    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    assertThat(reader.readLine(), equalTo("hello world"));
  }

  @Test
  public void readXZCompressed() throws IOException {
    String expected = "For if Jack Buggit could escape from the pickle jar";


    FileObject file = VFS.getManager().resolveFile(getClass().getResource("news.txt.xz").getFile());
    GzFileConnection conn = new GzFileConnection(file);

    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    assertThat(reader.readLine(), equalTo(expected));
  }


  @Test
  public void multipleReads() throws IOException {

    FileObject file = VFS.getManager().resolveFile(getClass().getResource("test2.txt").getFile());
    SEXP conn = topLevelContext.getSession().getConnectionTable().newConnection(new GzFileConnection(file));

    assertThat( Connections.readChar(topLevelContext, conn, 9, false), equalTo("The quick"));
    assertThat( Connections.readChar(topLevelContext, conn, 6, false), equalTo(" brown"));

  }


  @Test
  public void readUnCompressed() throws IOException {

    FileObject file = VFS.getManager().resolveFile(getClass().getResource("test.txt").getFile());
    GzFileConnection conn = new GzFileConnection(file);

    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    assertThat(reader.readLine(), equalTo("Hello again, dear world"));
  }

}
