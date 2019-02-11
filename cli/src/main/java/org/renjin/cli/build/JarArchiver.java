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
package org.renjin.cli.build;

import org.renjin.RenjinVersion;
import org.renjin.repackaged.guava.io.ByteStreams;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.jar.*;

/**
 * Creates a JAR from the package
 */
public class JarArchiver implements AutoCloseable {
  
  private static final Attributes.Name CREATED_BY = new Attributes.Name("Created-By");
  
  private JarOutputStream output;

  private Set<String> entries = new HashSet<>();

  public JarArchiver(File jarFile, Optional<String> executableNamespace) throws IOException {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(CREATED_BY, "Renjin " + RenjinVersion.getVersionName());

    executableNamespace.ifPresent(namespace -> {
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "org.renjin.script.Execute");
    });

    this.output = new JarOutputStream(new FileOutputStream(jarFile), manifest);
  }

  public void addDirectory(File dir) throws IOException {
    add(dir, dir);
  }
  
  private void add(File relativeTo, File source) throws IOException {
    JarEntry entry = new JarEntry(relative(relativeTo, source));
    entry.setTime(source.lastModified());

    entries.add(entry.getName());

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


  public void addClassesFromJar(File nestedJarFile) throws IOException {
    try(JarInputStream jarIn = new JarInputStream(new FileInputStream(nestedJarFile))) {
      JarEntry entryIn;
      while((entryIn = jarIn.getNextJarEntry()) != null) {
        if(!excludedFromDependencyJars(entryIn)) {

          if(entries.contains(entryIn.getName())) {
            if(!entryIn.isDirectory()) {
              System.err.println("WARNING: Excluding duplicate resource '" + entryIn.getName() + " from " + nestedJarFile.getName());
            }
          } else {
            JarEntry entry = new JarEntry(entryIn);
            output.putNextEntry(entry);

            ByteStreams.copy(jarIn, output);

            output.closeEntry();
            entries.add(entryIn.getName());
          }
        }
      }
    }

  }

  private boolean excludedFromDependencyJars(JarEntry entryIn) {
    if(entryIn.getName().equals("META-INF/MANIFEST.MF")) {
      return true;
    }
    if(entryIn.getName().startsWith("META-INF/org.renjin.execute")) {
      return true;
    }
    if(entryIn.getName().startsWith("META-INF/maven")) {
      return true;
    }
    return false;
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
