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
package org.renjin.util;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.apache.commons.vfs2.provider.UriParser;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.ImmutableList;

import java.net.URL;
import java.util.Collection;

/**
 * An alternative version of {@link org.apache.commons.vfs2.provider.res.ResourceFileProvider} that
 * accepts a {@link ClassLoader} as a constructor parameter.
 *
 */
public class ClasspathFileProvider extends AbstractFileProvider {

  private static final Collection<Capability> CAPABILITIES = ImmutableList.of(Capability.DISPATCHER);

  private final ClassLoader classLoader;

  public ClasspathFileProvider(ClassLoader classLoader) {
    super();
    Preconditions.checkNotNull(classLoader, "classloader");
    this.classLoader = classLoader;
  }

  /**
   * Locates a file object, by absolute URI.
   * @param baseFile The base file.
   * @param uri The URI of the file to locate.
   * @param fileSystemOptions The FileSystem options.
   * @return the FileObject.
   * @throws FileSystemException if an error occurs.
   */
  public FileObject findFile(final FileObject baseFile,
                             final String uri,
                             final FileSystemOptions fileSystemOptions) throws FileSystemException {

    StringBuilder buf = new StringBuilder(80);
    UriParser.extractScheme(uri, buf);
    String resourceName = buf.toString();

    final URL url = classLoader.getResource(resourceName);

    if (url == null)
    {
      throw new FileSystemException("vfs.provider.url/badly-formed-uri.error", uri);
    }

    return getContext().getFileSystemManager().resolveFile(url.toExternalForm());
  }

  @Override
  public FileSystemConfigBuilder getConfigBuilder() {
    return new DefaultFileSystemConfigBuilder();
  }

  @Override
  public void closeFileSystem(FileSystem filesystem) {
    // no filesystem created here - so nothing to do
  }

  public Collection<Capability> getCapabilities()
  {
    return CAPABILITIES;
  }
}
