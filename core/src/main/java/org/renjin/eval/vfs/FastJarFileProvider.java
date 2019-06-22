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

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractLayeredFileProvider;
import org.apache.commons.vfs2.provider.LayeredFileName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Fast(er) Replacement for the CommonsVFS Jar File provider.
 *
 * <p>The Commons VFS implementation starts by building a list of FileObjects
 * for all jar entries in the jar, even if they're never accessed. This seems
 * to be contributing to delays in start up time.</p>
 *
 */
public class FastJarFileProvider extends AbstractLayeredFileProvider {


  final static Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays.asList(
      Capability.GET_LAST_MODIFIED,
      Capability.GET_TYPE,
      Capability.LIST_CHILDREN,
      Capability.READ_CONTENT,
      Capability.URI,
      Capability.COMPRESS,
      Capability.VIRTUAL));


  /**
   * Creates a layered file system.  This method is called if the file system
   * is not cached.
   *
   * @param scheme The URI scheme.
   * @param file   The file to create the file system on top of.
   * @return The file system.
   */
  protected FileSystem doCreateFileSystem(final String scheme,
                                          final FileObject file,
                                          final FileSystemOptions fileSystemOptions)
      throws FileSystemException
  {
    final FileName rootName =
        new LayeredFileName(scheme, file.getName(), FileName.ROOT_PATH, FileType.FOLDER);
    return new FastJarFileSystem(rootName, file, fileSystemOptions);
  }

  public Collection<Capability> getCapabilities() {
    return capabilities;
  }
}
