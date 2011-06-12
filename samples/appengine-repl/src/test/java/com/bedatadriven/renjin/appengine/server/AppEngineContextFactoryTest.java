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

package com.bedatadriven.renjin.appengine.server;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.junit.Test;
import r.lang.Context;
import r.lang.Symbol;
import r.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AppEngineContextFactoryTest {

  @Test
  public void rootFile() throws IOException {
    DefaultLocalFileProvider localFileProvider = new DefaultLocalFileProvider();
    FileSystemManager fsm = AppEngineContextFactory.createFileSystemManager(localFileProvider);

    Context context = Context.newTopLevelContext(fsm, FileSystemUtils.homeDirectoryInCoreJar(),
        FileSystemUtils.workingDirectory(fsm));
    context.init();

    new Symbol("search").evaluate(context, context.getEnvironment());
  }

  @Test
  public void homeDirectory() throws IOException {
    String resourcePath = "file:/base/app/1.234234/WEB-INF/lib/renjin-core-0.1.0-SNAPSHOT.jar!/r/lang/SEXP.class";
    String home = AppEngineContextFactory.findHomeDirectory(
        new File("\\base\\app\\1.234234"), resourcePath);

    assertThat( home, equalTo("jar:file:///WEB-INF/lib/renjin-core-0.1.0-SNAPSHOT.jar!/r"));
  }

}
