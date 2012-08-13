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
  public void multipleReads() throws IOException {

    FileObject file = VFS.getManager().resolveFile(getClass().getResource("test2.txt").getFile());
    SEXP conn = topLevelContext.getGlobals().getConnectionTable().newConnection(new GzFileConnection(file));

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
