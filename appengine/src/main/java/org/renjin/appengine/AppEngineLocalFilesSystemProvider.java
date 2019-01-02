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

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.apache.commons.vfs2.provider.LocalFileProvider;
import org.apache.commons.vfs2.provider.UriParser;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Provides read-only access to the application context
 * on the local file system.
 *
 * <p>We have to be careful because trying to read or
 * touch parent folders (e.g. /base) will throw a SecurityException
 *
 */
public class AppEngineLocalFilesSystemProvider
    extends AbstractFileProvider implements
    LocalFileProvider, Comparable<AppEngineLocalFilesSystemProvider> {

  private File rootFile;

  public final static Collection<Capability> CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(
      Capability.GET_TYPE,
      Capability.GET_LAST_MODIFIED,
      Capability.LIST_CHILDREN,
      Capability.READ_CONTENT,
      Capability.URI,
      Capability.RANDOM_ACCESS_READ));

  public AppEngineLocalFilesSystemProvider(File rootFile) {
    this.rootFile = rootFile;
  }

  @Override
  public FileObject findLocalFile(String name) throws FileSystemException {
    return findFile(null, name, null);
  }

  @Override
  public FileObject findLocalFile(File file) throws FileSystemException {
    return findFile(null, file.getPath(), null);
  }

  @Override
  public FileObject findFile(FileObject baseFile, String uri,
                             FileSystemOptions properties) throws FileSystemException {

    // Parse the name
    final StringBuilder buffer = new StringBuilder(uri);
    String scheme = UriParser.extractScheme(uri, buffer);
    if(scheme == null) {
      scheme = "file";
    }

    UriParser.fixSeparators(buffer);

    FileType fileType = UriParser.normalisePath(buffer);
    final String path = buffer.toString();

    // Create the temp file system if it does not exist
    // FileSystem filesystem = findFileSystem( this, (Properties) null);
    FileSystem filesystem = findFileSystem(this, properties);
    if (filesystem == null)
    {
      final FileName rootName =
          getContext().parseURI(scheme + ":" + FileName.ROOT_PATH);

      filesystem = new AppEngineLocalFileSystem(rootName, rootFile.getAbsolutePath(), properties);
      addFileSystem(this, filesystem);
    }

    // Find the file
    return filesystem.resolveFile(path);
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return CAPABILITIES;
  }

  @Override
  public int compareTo(AppEngineLocalFilesSystemProvider o) {
    return hashCode() - o.hashCode();
  }

  @Override
  public boolean isAbsoluteLocalName(String name) {
    return name.startsWith("/");
  }
}
