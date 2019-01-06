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
package org.renjin.packaging;


import org.renjin.repackaged.guava.io.ByteStreams;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PackageBuilder {

  private PackageSource packageSource;
  private BuildContext context;

  private boolean ignoreNativeCompilationFailure;

  public PackageBuilder(PackageSource packageSource, BuildContext context) {
    this.packageSource = packageSource;
    this.context = context;
  }

  public void setIgnoreNativeCompilationFailure(boolean ignoreNativeCompilationFailure) {
    this.ignoreNativeCompilationFailure = ignoreNativeCompilationFailure;
  }

  public void build() throws IOException {
    copyRootFiles();
    compileNativeSources();
    copyInstalledFiles();
    compileNamespace();
    compileDatasets();
  }


  /**
   * Copies files from the package root, including DESCRIPTION and NAMESPACE
   */
  public void copyRootFiles() throws IOException {
    packageSource.getDescription().writeTo(new File(context.getPackageOutputDir(), "DESCRIPTION"));
    copyRootFile(packageSource.getNamespaceFile());
  }

  private void copyRootFile(File file) throws IOException {
    Files.copy(file, new File(context.getPackageOutputDir(), file.getName()));
  }

  private void compileNativeSources() {

    if(packageSource.getNativeSourceDir().exists()) {
      context.setupNativeCompilation();

      try {
        NativeSourceBuilder nativeSourceBuilder = new NativeSourceBuilder(packageSource, context);
        nativeSourceBuilder.build();
      } catch (Exception e) {
        if (ignoreNativeCompilationFailure) {
          context.getLogger().error("Compilation of GNU R sources failed.");
          e.printStackTrace(System.err);
        } else {
          throw new BuildException("Compilation of GNU R sources failed", e);
        }
      }
    }
  }

  /**
   * Copies files recursively from the inst/ directory to the packageOutputDir
   */
  private void copyInstalledFiles() throws IOException {

    if(packageSource.getInstalledFilesDir().exists()) {
      copyInstalledFiles(packageSource.getInstalledFilesDir(), context.getPackageOutputDir());
    }
  }

  private void copyInstalledFiles(File sourceDir, File targetDir) throws IOException {

    if(!targetDir.exists()) {
      boolean created = targetDir.mkdirs();
      if(!created) {
        throw new IOException("Failed to create output directory '" + targetDir.getAbsolutePath() + "'");
      }
    }

    File[] sourceFiles = sourceDir.listFiles();
    if(sourceFiles != null) {
      for (File sourceFile : sourceFiles) {
        File targetFile = new File(targetDir, sourceFile.getName());
        if(sourceFile.isDirectory()) {
          copyInstalledFiles(sourceFile, targetFile);
          
        } else if(isJarFile(sourceFile)) {
          mergeJar(sourceFile);
          
        } else {
          Files.copy(sourceFile, targetFile);
        }
      }
    }
  }

  private boolean isJarFile(File sourceFile) {
    if( sourceFile.getName().endsWith(".jar") &&
        sourceFile.getParentFile().getName().equals("java") &&
        sourceFile.getParentFile().getParentFile().getName().equals("inst")) {
      return true;
    }
    return false;
  }


  public void compileNamespace() throws IOException {
    NamespaceBuilder2 builder = new NamespaceBuilder2(packageSource, context);
    builder.compile();
  }


  public void compileDatasets() throws IOException {
    DatasetsBuilder2 build = new DatasetsBuilder2(packageSource, context);
    build.build();
  }

  /**
   * GNU R packages have a convention of including compiled Java JAR files in the 
   * package source, located in inst/java, which are then loaded by rJava at runtime.
   *
   * <p>If we merge the classes from those JARs into the JAR for this package, then
   * they will be on the normal classpath as expected at runtime and we don't have to mess 
   * around with the classpath later.</p>
   */
  private void mergeJar(File jarFile) throws IOException {
    try (JarInputStream in = new JarInputStream(new FileInputStream(jarFile))) {
      JarEntry entry;
      while ((entry = in.getNextJarEntry()) != null) {
        if (!entry.isDirectory()) {
          File outputFile = new File(context.getOutputDir().getAbsolutePath() + "/" + entry.getName());
          if(!outputFile.getParentFile().exists()) {
            boolean created = outputFile.getParentFile().mkdirs();
            if(!created) {
              throw new IOException("Failed to create " + outputFile.getParent());
            }
          }
          try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            ByteStreams.copy(in, outputStream);
          }
        }
      }
    }
  }
}
