/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.util;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.junit.Test;
import org.renjin.repackaged.guava.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ClasspathFileProviderTest {

  @Test
  public void nonAsciiCharacterTest() throws IOException {

    FileSystemManager fsm = FileSystemUtils.getMinimalFileSystemManager(getClass().getClassLoader());
    FileObject resourceObject = fsm.resolveFile("classpath:///téléchargements.txt");
    assertThat(resourceObject.exists(), equalTo(true));

    String content;
    try(InputStream in = resourceObject.getContent().getInputStream()) {
      content = new String(ByteStreams.toByteArray(in));
    }
    assertThat(content, equalTo("Ceci n'est pas une fiche."));
  }

}