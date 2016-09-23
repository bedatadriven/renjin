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
package org.renjin.cli.build;

import org.renjin.RenjinVersion;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Creates a JAR from the package
 */
public class JarArchiver implements AutoCloseable {
  
  private static final Attributes.Name CREATED_BY = new Attributes.Name("Created-By");
  
  public JarOutputStream output;

  public JarArchiver(File jarFile) throws IOException {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(CREATED_BY, "Renjin " + RenjinVersion.getVersionName());

    this.output = new JarOutputStream(new FileOutputStream(jarFile), manifest);
  }

  public void addDirectory(File dir) throws IOException {
    add(dir, dir);
  }
  
  private void add(File relativeTo, File source) throws IOException {
    JarEntry entry = new JarEntry(relative(relativeTo, source));
    entry.setTime(source.lastModified());
    
    output.putNextEntry(entry);

    if(source.isFile()) {
      Files.copy(source, output);
    }
    
    output.closeEntry();
    
    if(source.isDirectory()) {
      File[] children = source.listFiles();
      if(children != null) {
        for (File child : children) {
          add(relativeTo, child);
        }
      }
    }
  }
  
  public void addFile(File source, String fileNameInJar) throws IOException {
    Preconditions.checkArgument(source.isFile());
    
    JarEntry entry = new JarEntry(fileNameInJar);
    entry.setTime(source.lastModified());

    output.putNextEntry(entry);
    
    Files.copy(source, output);
  }

  private String relative(File relativeTo, File source) {
    String rootPath = relativeTo.getAbsolutePath().replace('\\', '/');
    String nestedPath = source.getAbsolutePath().replace('\\', '/');
    if(!nestedPath.startsWith(rootPath)) {
      throw new IllegalStateException(String.format("'%s' is not a child of '%s'", nestedPath, rootPath));
    }
    String path = nestedPath.substring(rootPath.length());
    while(path.startsWith("/")) {
      path = path.substring(1);
    }
    if(source.isDirectory() && !path.endsWith("/")) {
      path += "/";
    }
    return path;
  }

  @Override
  public void close() throws IOException {
    output.close();
  }
}
