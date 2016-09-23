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
package org.renjin.primitives.files;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.StringVector;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class FilesTest extends EvalTestCase {

  @Before
  public void setUpTests() {
    assumingBasePackagesLoad();
  }


  @Test
  @Ignore("setwd should throw error if path doesn't exist")
  public void getSetWd(){
    eval("older<-getwd()");
    eval("setwd('/path/to/file')");
    assertThat(eval("getwd()"), equalTo(c("file:///path/to/file")));
    eval("setwd(older)");
  }

  @Test
  public void listFiles() throws URISyntaxException, FileSystemException {

    // For reproducible tests, we've included a hierarchy of files in src/test/resources
    // These should be on the classpath when tests are run
    URL resourceURL = FilesTest.class.getResource("FilesTest/a.txt");
    FileObject rootDir = topLevelContext.resolveFile(resourceURL.getPath()).getParent();

    topLevelContext.getGlobalEnvironment().setVariable("rootDir", StringVector.valueOf(rootDir.toString()));

    assertThat(eval("list.files(rootDir)"), equalTo(c("a.txt", "b.txt", "c")));

    assertThat(eval("list.files(rootDir, all.files=TRUE)"), equalTo(c(".", "..", ".hidden.txt", "a.txt", "b.txt", "c")));

    assertThat(eval("list.files(rootDir, pattern='txt$')"), equalTo(c("a.txt", "b.txt")));

    assertThat(eval("list.files(rootDir, pattern='TXT$', ignore.case=TRUE)"), equalTo(c("a.txt", "b.txt")));

    assertThat(eval("list.files(rootDir, pattern='txt$', all.files=TRUE)"),
        equalTo(c(".hidden.txt", "a.txt", "b.txt")));

    assertThat(eval("list.files(rootDir, pattern='txt$', recursive=TRUE)"),
        equalTo(c("a.txt", "b.txt", "c/ca.txt", "c/cb.txt", "c/d/cda.txt")));

    assertThat(eval("list.files(rootDir, pattern='c', recursive=TRUE, include.dirs=TRUE)"),
        equalTo(c("c", "c/ca.txt", "c/cb.txt", "c/d/cda.txt")));
  }

  @Test
  public void renameFiles() throws IOException {

    // Create a temp directory with a source file
    File tempDir = org.renjin.repackaged.guava.io.Files.createTempDir();
    File sourceFile = new File(tempDir, "a.txt");
    File destFile = new File(tempDir, "b.txt");
    Files.write("ABC", sourceFile, Charsets.UTF_8);

    topLevelContext.getGlobalEnvironment().setVariable("rootDir", StringVector.valueOf(tempDir.getAbsolutePath()));
    
    eval("setwd(rootDir)");
    eval("x <- file.rename('a.txt', 'b.txt')");
    
    assertThat(eval("x"), equalTo(c(true)));
    
    assertTrue("source file does not exist", !sourceFile.exists());
    assertTrue("dest file exists", destFile.exists());
  }

  @Test
  public void renameFilesFailure() throws IOException {

    // Failure returns false, does not throw error
    
    eval("x <- file.rename('doesnotexist.txt', 'b.txt')");

    assertThat(eval("x"), equalTo(c(false)));

  }

}
