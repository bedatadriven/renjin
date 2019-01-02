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

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.apache.commons.vfs2.provider.UriParser;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.ImmutableList;

import java.net.URL;
import java.util.Collection;

/**
 * An alternative version of {@link org.apache.commons.vfs2.provider.res.ResourceFileProvider} that
 * accepts a {@link ClassLoader} as a constructor parameter, and handles jars nested in jars.
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

    String normalizeUri = normalizeNestedJarUris(url.toExternalForm());

    return getContext().getFileSystemManager().resolveFile(normalizeUri);
  }


  /**
   * Some ClassLoaders, which apparently includes Spring Boot, generate URLs for nested jar that look like:
   * <pre>
   *   jar:file:/path/to/web.jar!/BOOT-INF/lib/renjin.jar!/org/renjin/sexp/SEXP.class
   * </pre>
   *
   * Instead of the form that VFS expects, which would be:
   * <pre>
   *   jar:jar:file:/path/to/web.jar!/BOOT-INF/lib/renjin.jar!/org/renjin/sexp/SEXP.class
   * </pre>
   *
   * This function normalizes urls with a single "jar:file" to the form expected by Apache VFS.
   *
   */
  @VisibleForTesting
  public static String normalizeNestedJarUris(String uri) {

    if(!uri.startsWith("jar:file:")) {
      return uri;
    }

    String[] parts = uri.split("!");

    StringBuilder normalized = new StringBuilder();
    normalized.append(parts[0]);
    normalized.append("!");

    for (int i = 1; i < parts.length - 1; i++) {
      normalized.insert(0, "jar:");
      normalized.append(parts[i]);
      normalized.append("!");
    }

    if(parts.length > 1) {
      normalized.append(parts[parts.length - 1]);
    }

    return normalized.toString();
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
