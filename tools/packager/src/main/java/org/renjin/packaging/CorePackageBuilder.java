import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repackaged.guava.base.Strings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

`` ``/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

    package org.renjin.packaging;

public class CorePackageBuilder implements BuildContext {


  public static void main(String[] args) throws IOException {

    File packageDir = new File(".").getAbsoluteFile().getCanonicalFile();

    System.out.println("PACKAGEDIR = " + packageDir.getName());

    PackageSource source = new PackageSource.Builder(new File("."))
        .setDefaultGroupId("org.renjin")
        .setPackageName(packageDir.getName())
        .build();


    if(source.getPackageName().isEmpty() || source.getPackageName().equals(".")) {
      throw new RuntimeException("packageName = " + source.getPackageName());
    }

    CorePackageBuilder context = new CorePackageBuilder(source);
    PackageBuilder builder = new PackageBuilder(source, context);
    builder.build();
  }

  private final PackageSource source;

  public CorePackageBuilder(PackageSource source) {
    this.source = source;
  }


  @Override
  public BuildLogger getLogger() {
    return new BuildLogger() {
      @Override
      public void info(String message) {
        System.err.println("[INFO] " + message);
      }

      @Override
      public void debug(String message) {
        System.err.println("[DEBUG] " + message);
      }

      @Override
      public void error(String message) {
        System.err.println("[ERROR] " + message);
      }

      @Override
      public void error(String message, Exception e) {
        System.err.println("[ERR] " + message);
        e.printStackTrace(System.err);
      }
    };
  }

  @Override
  public void setupNativeCompilation() {
  }

  @Override
  public File getGccBridgePlugin() {
    return new File(".");
  }

  @Override
  public File getGnuRHomeDir() {
    String home = System.getenv("R_HOME");
    if(Strings.isNullOrEmpty(home)) {
      throw new RuntimeException("Environment variable R_HOME not set");
    }
    File homeDir = new File(home);
    if(!homeDir.exists()) {
      throw new RuntimeException("R_HOME dir " + homeDir.getAbsolutePath() + " does not exist");
    }
    return homeDir;
  }

  @Override
  public File getUnpackedIncludesDir() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public File getOutputDir() {
    return new File("build/namespace/classes");
  }

  @Override
  public File getCompileLogDir() {
    return new File("build/gcc-bridge/logs");
  }

  @Override
  public File getPackageOutputDir() {
    return new File("build/namespace/classes/" +
        source.getJavaPackageName().replace('.', '/'));
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
  public String getCompileClasspath() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Map<String, String> getPackageGroupMap() {
    return Collections.emptyMap();
  }
}
