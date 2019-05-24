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

import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.gnur.GlobalVarPlugin;
import org.renjin.gnur.GnurSourcesCompiler;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repackaged.guava.io.LineProcessor;
import soot.G;
import soot.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Builds the native library part of a GNU R package, using {@code make} and any
 * provided {@code Makevars} file.
 */
public class NativeSourceBuilder {

  private static final List<String> SOURCE_EXTENSIONS = Lists.newArrayList(
      "c",
      "f", "f77", "f90", "f95", "f03", "for",
      "cpp", "cxx", "cc");

  private PackageSource source;
  private BuildContext buildContext;
  private boolean transformGlobalVariables;

  public NativeSourceBuilder(PackageSource source, BuildContext buildContext) {
    this.source = source;
    this.buildContext = buildContext;
  }

  /**
   * Determines whether we attempt to transform global variables to Renjin-context scoped global variables.
   */
  public void setTransformGlobalVariables(boolean transformGlobalVariables) {
    this.transformGlobalVariables = transformGlobalVariables;
  }

  public void build() throws IOException, InterruptedException {
    configure();
    make();
    compileGimple();
    buildContext.getLogger().info("Compilation of GNU R sources succeeded.");

//    optimizeClasses();

    // When soot hits an error, it can set the interrupt flag for this
    // thread.
    Thread.interrupted();

  }

  private void configure() throws IOException, InterruptedException {

    File renjinMakeVars = new File(source.getNativeSourceDir(), "Makevars.renjin");
    if (renjinMakeVars.exists()) {
      buildContext.getLogger().debug("Makevars.renjin exists, skipping ./configure...");
    }

    File configure = new File(source.getPackageDir(), "configure");
    if(!configure.exists()) {
      buildContext.getLogger().debug("No ./configure script found at " + configure.getAbsolutePath() +
          ", skipping...");
      return;
    }

    buildContext.getLogger().debug("Running Configure...");

    // Setup process
    ProcessBuilder builder = new ProcessBuilder()
        .command("sh", "configure")
        .directory(source.getPackageDir())
        .inheritIO();

    builder.environment().put("R_HOME", buildContext.getGnuRHomeDir().getAbsolutePath());

    int exitCode = builder.start().waitFor();
    if (exitCode != 0) {
      throw new InternalCompilerException("Failed to execute ./configure");
    }
  }

  private void make() throws IOException, InterruptedException {

    File makeconfFile = new File(buildContext.getGnuRHomeDir().getAbsolutePath() + "/etc/Makeconf");
    File shlibMk = new File(buildContext.getGnuRHomeDir().getAbsolutePath() + "/share/make/shlib.mk");

    List<String> commandLine = Lists.newArrayList();
    commandLine.add("make");

    // Combine R's default Makefile with package-specific Makevars if present
    File makevars = new File(source.getNativeSourceDir(), "Makevars.renjin");
    if (!makevars.exists()) {
      makevars =  new File(source.getNativeSourceDir(), "Makevars");
    }
    if (makevars.exists()) {
      commandLine.add("-f");
      commandLine.add(makevars.getName());
    }

    // Makeconf file
    commandLine.add("-f");
    commandLine.add(makeconfFile.getAbsolutePath());

    commandLine.add("-f");
    commandLine.add(shlibMk.getAbsolutePath());

    commandLine.add("SHLIB='" + source.getPackageName() + ".so'");

    if(!definedByMakeVars(makevars, "^OBJECTS\\s*=")) {
      commandLine.add("OBJECTS=" + findObjectFiles());
    }
    commandLine.add("BRIDGE_PLUGIN=" + buildContext.getGccBridgePlugin().getAbsolutePath());

    // Packages using native code can defined the C++ standard in DESCRIPTION file
    // SystemRequirements fields or in Makevars file as CXX_STD variable. Using this
    // information, GNU R (src/library/tools/R/install.R), overwrites the flags as
    // defined in Makeconf. In Renjin, we check whether if the CXX11 flag is set in
    // DESCRIPTION or Makevars and overwrite the flags that install.R would otherwise
    // overwrite. We should add other cases from install.R ones we move to new version
    // of gcc that supports C++14 and C++17.
    if(definedByMakeVars(makevars, "(CXX_STD)\\W+(CXX11)") || source.isCXX11()) {
      if(!source.isCXX11()) {
        System.out.println("Checking wether in Makevars CXX_STD is set to CXX11... yes");
      }
      commandLine.add("CXX=$(CXX11) $(CXX11STD)");
      commandLine.add("CXXFLAGS=$(CXX11FLAGS)");
      commandLine.add("CXXPICFLAGS=$(CXX11PICFLAGS)");
      commandLine.add("SHLIB_LDFLAGS=$(SHLIB_CXX11LDFLAGS)");
      commandLine.add("SHLIB_LD=$(SHLIB_CXX11LD)");
    } else {
      System.out.println("Checking wether in Makevars CXX_STD is set to CXX11... no");
    }

    buildContext.getLogger().debug("Executing " + Joiner.on(" ").join(commandLine));

    // Setup process
    ProcessBuilder builder = new ProcessBuilder()
        .command(commandLine)
        .directory(source.getNativeSourceDir())
        .inheritIO();

    builder.environment().put("R_VERSION", "3.2.0");
    builder.environment().put("R_HOME", buildContext.getGnuRHomeDir().getAbsolutePath());
    builder.environment().put("R_INCLUDE_DIR", buildContext.getGnuRHomeDir().getAbsolutePath() + "/include");
    builder.environment().put("R_SHARE_DIR", buildContext.getGnuRHomeDir().getAbsolutePath() + "/share");

    builder.environment().put("R_PACKAGE_NAME", source.getPackageName());
    builder.environment().put("R_INSTALL_PACKAGE", source.getPackageName());
    builder.environment().put("R_PACKAGE_DIR", buildContext.getPackageOutputDir().getAbsolutePath());

    builder.environment().put("CLINK_CPPFLAGS", "-I\"" + buildContext.getUnpackedIncludesDir().getAbsolutePath() + "\"");

    // Provide sensible defaults for locations of system tools
    if(!builder.environment().containsKey("MAKE")) {
      builder.environment().put("MAKE", "make");
    }

    if(!builder.environment().containsKey("R_UNZIPCMD")) {
      builder.environment().put("R_UNZIPCMD", "/usr/bin/unzip");
    }

    if(!builder.environment().containsKey("R_GZIPCMD")) {
      builder.environment().put("R_GZIPCMD", "/usr/bin/gzip");
    }

    int exitCode = builder.start().waitFor();
    if (exitCode != 0) {
      throw new InternalCompilerException("Failed to execute Makefile");
    }
  }

  private void compileGimple() {

    List<GimpleCompilationUnit> gimpleFiles = Lists.newArrayList();
    collectGimple(source.getNativeSourceDir(), gimpleFiles);

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setLinkClassLoader(buildContext.getClassLoader());
    compiler.setOutputDirectory(buildContext.getOutputDir());
    compiler.setPackageName(source.getGroupId() + "." +
        Namespace.sanitizePackageNameForClassFiles(source.getPackageName()));
    compiler.setClassName(findLibraryName());
    compiler.setLoggingDirectory(buildContext.getCompileLogDir());

    try {
      GnurSourcesCompiler.setupCompiler(compiler);
    } catch (ClassNotFoundException e) {
      throw new BuildException("Failed to setup Gimple Compiler", e);
    }

    if(transformGlobalVariables) {
      compiler.addPlugin(new GlobalVarPlugin(compiler.getPackageName()));
    }

    try {
      compiler.compile(gimpleFiles);
    } catch (Exception e) {
      throw new BuildException("Failed to compile Gimple", e);
    }
  }

  private void optimizeClasses() {

    List<String> classes = new ArrayList<>();
    findClassfiles(buildContext.getOutputDir(), "", classes);

    List<String> args = new ArrayList<>();
    // Preprend maven's classpath
    args.add("-pp");

    // Classpath for soot analysis
    args.add("-cp");
    args.add(buildContext.getSootClasspath());

    args.add("-asm-backend");
    args.add("-java-version");
    args.add("1.8");

    // Add classes to optimize
    args.add("-O");
    args.addAll(classes);

    // Write out to build directory and overwrite existing classfiles
    args.add("-d");
    args.add(buildContext.getOutputDir().getAbsolutePath());

    G.reset();

    Main.v().run(args.toArray(new String[args.size()]));
  }


  private void findClassfiles(File file, String packageName, List<String> classes) {
    for (File child : file.listFiles()) {
      if(child.isFile() && child.getName().endsWith(".class")) {
        classes.add(packageName + Files.getNameWithoutExtension(child.getName()));
      } else if(child.isDirectory()) {
        findClassfiles(child, packageName + child.getName() + ".", classes);
      }
    }
  }


  private String findLibraryName() {
    // Packages can rename the shared library after it's build.
    // (Yes, looking at you data.table!!)

    for (File file : source.getNativeSourceDir().listFiles()) {
      String filename = file.getName();
      if(filename.endsWith(".so")) {
        return Namespace.sanitizePackageNameForClassFiles(filename.substring(0, filename.length() - ".so".length()));
      }
    }

    // Otherwise assume it's the same as the package name
    return Namespace.sanitizePackageNameForClassFiles(source.getPackageName());

  }


  private boolean definedByMakeVars(File makevars, String pattern) throws IOException {
    if(!makevars.exists()) {
      return false;
    }

    final Pattern definitionRegexp = Pattern.compile(pattern);

    return Files.readLines(makevars, Charsets.UTF_8, new LineProcessor<Boolean>() {

      private boolean defined = false;

      @Override
      public boolean processLine(String line) {
        if(definitionRegexp.matcher(line).find()) {
          defined = true;
          return false;
        }
        // Keep processing
        return true;
      }

      @Override
      public Boolean getResult() {
        return defined;
      }
    });
  }

  private String findObjectFiles() {
    List<String> objectFiles = new ArrayList<>();
    File[] files = source.getNativeSourceDir().listFiles();
    if(files != null)  {
      for (File file : files) {
        String extension = Files.getFileExtension(file.getName());
        if(SOURCE_EXTENSIONS.contains(extension)) {
          String baseName = Files.getNameWithoutExtension(file.getName());
          objectFiles.add(baseName + ".o");
        }
      }
    }
    return Joiner.on(" ").join(objectFiles);
  }

  private void collectGimple(File dir, List<GimpleCompilationUnit> gimpleFiles) {

    GimpleParser parser = new GimpleParser();

    File[] files = dir.listFiles();
    if(files != null) {
      for (File file : files) {
        if(file.getName().endsWith(".gimple")) {
          buildContext.getLogger().debug("Reading " + 
              file.getAbsolutePath().substring(source.getNativeSourceDir().getAbsolutePath().length()));
          try {
            gimpleFiles.add(parser.parse(file));
          } catch (IOException e) {
            throw new BuildException("Failed to parse gimple file " + file, e);
          }
        } else if(file.isDirectory()) {
          collectGimple(file, gimpleFiles);
        }
      }
    }
  }

}
