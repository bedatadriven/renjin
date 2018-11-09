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
package org.renjin.eval.vfs;


import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.junit.Before;
import org.junit.Test;
import org.renjin.util.ClasspathFileProvider;

import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FastJarFileProviderTest {


  private DefaultFileSystemManager fsm;

  @Before
  public void setUp() throws Exception {
    fsm = new DefaultFileSystemManager();
    fsm.setDefaultProvider(new UrlFileProvider());
    fsm.setReplicator(new DefaultFileReplicator());
    fsm.addProvider("file", new DefaultLocalFileProvider());
    fsm.addProvider("jar", new FastJarFileProvider());
    fsm.init();
  }

  @Test
  public void test() throws FileSystemException, URISyntaxException {

    String jarUri = getClass().getResource("/jarfiletest.jar").toURI().toString();
    FileObject object = fsm.resolveFile("jar:" + jarUri + "!/r/");

    assertThat(object.exists(), equalTo(true));
    assertThat(object.getType(), equalTo(FileType.FOLDER));

    FileObject[] children = object.getChildren();
    assertThat(children.length, equalTo(1));
    assertThat(children[0].getName().getBaseName(), equalTo("library"));
    assertThat(children[0].getType(), equalTo(FileType.FOLDER));

    object = fsm.resolveFile("jar:" + jarUri + "!/r/library/survey");

    assertThat(object.getType(), equalTo(FileType.FOLDER));
    assertThat(object.getChildren().length, equalTo(4));
  }

  /**
   * This is the format that VFS expects:
   *
   * jar:jar:file://outer.jar!/inner.jar!path/to/file
   */
  @Test
  public void nestedUris() throws FileSystemException {

    URL jarResource = FastJarFileProvider.class.getResource("web.jar");

    String jarUrl = jarResource.toString();

    FileObject jarFileObject = fsm.resolveFile(jarUrl);
    if(!jarFileObject.exists()) {
      throw new AssertionError("Something is wrong, can't find the test resource web.jar at " + jarUrl);
    }

    String innerJarUrl = "jar:" + jarUrl + "!/BOOT-INF/lib/renjin.jar";
    String classFileUrl = "jar:" + innerJarUrl + "!/org/renjin/sexp/SEXP.class";

    FileObject classFileObject = fsm.resolveFile(classFileUrl);

    if(!classFileObject.exists()) {
      throw new AssertionError("Inner jar at " + innerJarUrl + " does not exist.");
    }
  }

  @Test
  public void recursive() throws FileSystemException {


    URL jarResource = FastJarFileProvider.class.getResource("web.jar");

    String jarUrl = jarResource.toString();

    String innerJarUrl = "jar:" + jarUrl + "!/BOOT-INF/lib/renjin.jar";
    String classFileUrl = innerJarUrl + "!/org/renjin/sexp/SEXP.class";

    FileObject innerJarFileObject = fsm.resolveFile(ClasspathFileProvider.normalizeNestedJarUris(classFileUrl));

    if(!innerJarFileObject.exists()) {
      throw new AssertionError("Inner jar at " + innerJarUrl + " does not exist.");
    }
  }

}
