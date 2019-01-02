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
package org.renjin.eval.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.io.InputStream;


public class FastJarRootFileObject extends AbstractFileObject {

  private FastJarFileSystem fs;

  protected FastJarRootFileObject(AbstractFileName name, FastJarFileSystem fs) {
    super(name, fs);
    this.fs = fs;
  }

  @Override
  protected FileType doGetType() throws Exception {
    return FileType.FOLDER;
  }

  @Override
  protected String[] doListChildren() throws Exception {
    return fs.getRootItems();
  }

  @Override
  protected long doGetContentSize() throws Exception {
    return 0;
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    throw new FileSystemException("can't open directory!");
  }

}
