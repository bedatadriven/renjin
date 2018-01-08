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
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FastJarFileProviderTest {

  @Test
  public void test() throws FileSystemException, URISyntaxException {
    DefaultFileSystemManager fsm = new DefaultFileSystemManager();
    fsm.setDefaultProvider(new UrlFileProvider());
    fsm.addProvider("file", new DefaultLocalFileProvider());
    fsm.addProvider("jar", new FastJarFileProvider());
    fsm.init();

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

}
