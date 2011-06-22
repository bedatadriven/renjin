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

package org.apache.commons.vfs.provider.jar;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * An overriden version of JarFileSystem that does not rely on
 * WeakRefFilesCache
 */
public class AppEngineJarFileSystem extends JarFileSystem {

  private Map<FileName,FileObject> cache = new HashMap<FileName,FileObject>();

  public AppEngineJarFileSystem(FileName rootName, FileObject file, FileSystemOptions fileSystemOptions) throws FileSystemException {
    super(rootName, file, fileSystemOptions);
  }

  @Override
  protected void putFileToCache(FileObject file) {
    cache.put(file.getName(), file);
  }

  @Override
  protected FileObject getFileFromCache(FileName name) {
    return cache.get(name);
  }

  @Override
  protected void removeFileFromCache(FileName name) {
    cache.remove(name);
  }
}
