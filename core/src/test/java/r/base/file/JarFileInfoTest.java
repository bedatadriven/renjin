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

package r.base.file;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class JarFileInfoTest {

  @Test
  public void directoryFromURL() {

    JarFileInfo file = new JarFileInfo( makeURL("/r/library") );

    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    assertFalse(file.isFile());
    assertThat(file.getName(), equalTo("library"));

    List<FileInfo> children = file.listFiles();
    assertThat(children.size(), equalTo(1));

    FileInfo survey = children.get(0);
    assertThat(survey.getName(), equalTo("survey"));
    assertThat(survey.isDirectory(), equalTo(true));
    assertThat(survey.exists(), equalTo(true));

    assertThat(survey.listFiles().size(), equalTo(4));

    FileInfo child = survey.getChild("NAMESPACE");
    assertThat(child.exists(), equalTo(true));
    assertThat(child.isFile(), equalTo(true));
    assertThat(child.getName(), equalTo("NAMESPACE"));

    FileInfo rechild = FileSystem.getFileInfo(child.getPath());
    assertThat(rechild.exists(), equalTo(true));


  }

  private String makeURL(String entryName) {
    return "jar:file:" + getClass().getResource("/jarfiletest.jar").getFile() + "!" + entryName;
  }

  @Test
  public void fileFromUrl() {
    JarFileInfo file = new JarFileInfo( makeURL("/r/library/survey/NEWS") );

    assertThat(file.exists(), equalTo(true));
    assertThat(file.isDirectory(), equalTo(false));
    assertThat(file.getName(), equalTo("NEWS"));
    assertThat(file.length(), equalTo(32182l));
    assertThat(file.listFiles().isEmpty(), equalTo(true));
  }

  @Test
  public void nonexistentFiles() {
    JarFileInfo file = new JarFileInfo( makeURL("/r/doesntexist/survey/NEWS") );
    assertThat(file.exists(), equalTo(false));
    assertThat(file.isFile(), equalTo(false));
    assertThat(file.isDirectory(), equalTo(false));
    assertThat(file.getName(), equalTo("NEWS"));
    assertThat(file.getPath(), equalTo(makeURL("/r/doesntexist/survey/NEWS")));

    JarFileInfo child = file.getChild("foobar");
    assertThat(child.exists(), equalTo(false));
    assertThat(child.getPath() ,equalTo(makeURL("/r/doesntexist/survey/NEWS/foobar")));
  }

  @Test
  public void whatDoesJavaIoFileDo() {
    assertThat(new File("/doesntexist").isFile(), equalTo(false));
  }

}
