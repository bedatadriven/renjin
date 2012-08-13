package org.renjin.appengine;
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



import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AppEngineLocalFilesSystemProviderTest {


  @Test
  public void test() throws FileSystemException {

    File basePath = new File(getClass().getResource("/jarfiletest.jar").getFile())
                      .getParentFile();

    FileSystemManager dfsm = AppEngineContextFactory.createFileSystemManager(
        new AppEngineLocalFilesSystemProvider(basePath));

    FileObject jarFile = dfsm.resolveFile("/jarfiletest.jar");
    assertThat(jarFile.getName().getURI(), equalTo("file:///jarfiletest.jar"));
    assertThat(jarFile.exists(), equalTo(true));

    FileObject jarRoot = dfsm.resolveFile("jar:file:///jarfiletest.jar!/r/library");
    assertThat(jarRoot.exists(), equalTo(true));
    assertThat(jarRoot.getType(), equalTo(FileType.FOLDER));
    assertThat(jarRoot.getChildren().length, equalTo(1));
  }
}
