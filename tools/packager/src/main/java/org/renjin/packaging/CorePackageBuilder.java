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

import org.renjin.RenjinVersion;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repackaged.guava.base.Strings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CorePackageBuilder implements BuildContext {

  static {
    // Silence warnings about not being able to load native libs
    Logger.getLogger("com.github.fommil.netlib").setLevel(Level.SEVERE);
  }

  public static void main(String[] args) throws IOException {

    PackageSource source = new PackageSource.Builder(new File("."))
        .setDefaultGroupId(detectGroupId())
        .setPackageName(packageNameFromWorkingDirectory())
        .setSourceDir(detectSourcesDirectory())
        .setNativeSourceDir(detectNativeSourceDir())
        .setDataDir(detectDataDir())
        .setDescription(buildDescription())
        .build();

    CorePackageBuilder context = new CorePackageBuilder(source);
    PackageBuilder builder = new PackageBuilder(source, context);
    builder.setTransformGlobalVariables("TRUE".equals(System.getenv("TRANSFORM_GLOBAL_VARIABLES")));
    builder.build();
  }

  private static PackageDescription buildDescription() throws IOException {

    File descriptionFile = new File("DESCRIPTION");

    PackageDescription description;
    if(descriptionFile.exists()) {
      description =  PackageDescription.fromFile(descriptionFile);
    } else {
      description = new PackageDescription();
    }

    // Override with settings from project
    description.setPackage(packageNameFromWorkingDirectory());
    description.setVersion(RenjinVersion.getVersionName());
    description.setProperty("GroupId", detectGroupId());

    return description;
  }

  private static String detectGroupId() {
    String groupId = System.getenv("PACKAGE_GROUP_ID");
    if(Strings.isNullOrEmpty(groupId)) {
      groupId = "org.renjin";
    }
    return groupId;
  }


  public static String packageNameFromWorkingDirectory() {
    try {
      File packageDir = new File(".").getAbsoluteFile().getCanonicalFile();
      return packageDir.getName();
    } catch (IOException e) {
      throw new IllegalStateException("Could not get package name from working directory", e);
    }
  }

  private static File detectSourcesDirectory() {
    File sourceDir = new File("src/main/R");
    if(sourceDir.exists() && sourceDir.isDirectory()) {
      return sourceDir;
    }
    return new File("R");
  }

  private static File detectDataDir() {
    File dataDir = new File("src/main/data");
    if(dataDir.exists() && dataDir.isDirectory()) {
      return dataDir;
    }
    return new File("data");
  }


  private static File detectNativeSourceDir() {
    File nativeSourceDir = new File("src/main/c");
    if(nativeSourceDir.exists()) {
      return nativeSourceDir;
    }
    return new File("src");
  }

  private final PackageSource source;

  public CorePackageBuilder(PackageSource source) {
    this.source = source;
  }


  @Override
  public BuildLogger getLogger() {
    return new SimpleLogger();
  }

  @Override
  public void setupNativeCompilation() {
  }

  @Override
  public File getGccBridgePlugin() {
    return readFileFromEnvironment("GCC_BRIDGE_PLUGIN");
  }

  @Override
  public File getGnuRHomeDir() {
    return readFileFromEnvironment("R_HOME");
  }

  private File readFileFromEnvironment(String name) {
    String home = System.getenv(name);
    if(Strings.isNullOrEmpty(home)) {
      throw new RuntimeException("Environment variable " + name + " not set");
    }
    File homeDir = new File(home);
    if(!homeDir.exists()) {
      throw new RuntimeException("File specified by " + name + " (" + homeDir.getAbsolutePath() + ") does not exist");
    }
    return homeDir;
  }

  @Override
  public File getUnpackedIncludesDir() {
    return new File("build/unpacked");
  }

  @Override
  public File getOutputDir() {
    return new File("build/namespace");
  }

  @Override
  public File getCompileLogDir() {
    return new File("build/gcc-bridge/logs");
  }

  @Override
  public File getPackageOutputDir() {
    return new File("build/namespace/" +
        source.getGroupId().replace('.', '/') + "/" +
        source.getPackageName());
  }

  @Override
  public PackageLoader getPackageLoader() {
    return new ClasspathPackageLoader(getClassLoader());
  }

  @Override
  public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  @Override
  public List<String> getDefaultPackages() {
    String defaultPackages = System.getProperty("defaultPackages", "");
    if(Strings.isNullOrEmpty(defaultPackages)) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(defaultPackages.split("\\s+"));
    }
  }

    @Override
  public Map<String, String> getPackageGroupMap() {
    return Collections.emptyMap();
  }

}
