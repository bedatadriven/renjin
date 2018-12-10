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
package org.renjin.cli.build;

import org.codehaus.plexus.util.FileUtils;
import org.renjin.eval.Session;
import org.renjin.packaging.BuildContext;
import org.renjin.packaging.BuildException;
import org.renjin.packaging.BuildLogger;
import org.renjin.packaging.PackageSource;
import org.renjin.primitives.packaging.PackageLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PackageBuild implements BuildContext {

  private final CliBuildLogger logger = new CliBuildLogger();

  private final File buildDir;
  private final PackageSource source;
  private final DependencyResolution dependencyResolution;
  private final File outputDir;
  private final File packageOutputDir;
  private final File mavenMetaDir;


  public PackageBuild(PackageSource source, Optional<String> renjinVersion) {
    this.buildDir = createCleanBuildDir(source.getPackageDir());
    this.source = source;
    this.outputDir = new File(buildDir, "classes");
    this.packageOutputDir = new File(
        outputDir + File.separator +
            source.getGroupId().replace('.', File.separatorChar) + File.separator +
            source.getPackageName());

    mkdirs(packageOutputDir);

    this.mavenMetaDir = new File(
        outputDir + File.separator +
            "META-INF" + File.separator +
            "maven" + File.separator +
            source.getGroupId() + File.separator +
            source.getPackageName());

    mkdirs(mavenMetaDir);

    this.dependencyResolution = new DependencyResolution(logger, source.getDescription(), renjinVersion);
  }

  public DependencyResolution getDependencyResolution() {
    return dependencyResolution;
  }

  public String getBuildVersion() {
    return source.getVersion();
  }

  private static File createCleanBuildDir(File packageDir) {
    File buildDir = new File(packageDir, "build");
    if(buildDir.exists()) {
      try {
        FileUtils.deleteDirectory(buildDir);
      } catch (IOException e) {
        throw new BuildException("Failed to delete build dir", e);
      }
    }
    boolean created = buildDir.mkdirs();
    if(!created) {
      throw new BuildException("Failed to create build dir");
    }
    return buildDir;
  }

  private void mkdirs(File dir) {
    if(!dir.exists()) {
      boolean created = dir.mkdirs();
      if(!created) {
        throw new BuildException("Failed to create " + dir.getAbsolutePath());
      }
    }
  }

  public PackageSource getSource() {
    return source;
  }

  @Override
  public BuildLogger getLogger() {
    return logger;
  }

  @Override
  public void setupNativeCompilation() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public File getGccBridgePlugin() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public File getGnuRHomeDir() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public File getUnpackedIncludesDir() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public File getOutputDir() {
    return outputDir;
  }

  @Override
  public File getPackageOutputDir() {
    return packageOutputDir;
  }

  public File getMavenMetaDir() {
    return mavenMetaDir;
  }

  public File getJarFile() {
    return new File(buildDir, source.getPackageName() + "-" + getBuildVersion() + ".jar");
  }

  public File getZipFile() {
    return new File(buildDir, source.getPackageName() + "-" + getBuildVersion() + ".zip");
  }

  public File getFatJarFile() {
    return new File(buildDir, source.getPackageName() + "-" + getBuildVersion() + "-fat.jar");
  }

  @Override
  public File getCompileLogDir() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PackageLoader getPackageLoader() {
    return dependencyResolution.getPackageLoader();
  }

  @Override
  public ClassLoader getClassLoader() {
    return dependencyResolution.getClassLoader();
  }

  @Override
  public List<String> getDefaultPackages() {
    return Session.DEFAULT_PACKAGES;
  }

  @Override
  public String getCompileClasspath() {
    return "";
  }

  @Override
  public Map<String, String> getPackageGroupMap() {
    return dependencyResolution.getPackageGroupMap();
  }



}
