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
package org.renjin.appengine;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.commons.vfs2.provider.local.LocalFileSystem;

/**
 * Wraps a LocalFileObject to avoid triggering SecurityExceptions
 *
 */
public class AppEngineLocalFile extends LocalFile {

  public AppEngineLocalFile(LocalFileSystem fileSystem, String rootFile,
      AbstractFileName name) throws FileSystemException {
    super(fileSystem, rootFile, name);
  }

  @Override
  protected boolean doIsWriteable() throws FileSystemException {
    return false;
  }
    
}
