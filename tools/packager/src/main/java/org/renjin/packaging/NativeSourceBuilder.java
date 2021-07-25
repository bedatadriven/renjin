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
import org.renjin.repackaged.guava.io.Resources;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    if(buildContext.getMakeStrategy() == MakeStrategy.VAGRANT) {
      buildWithVagrant();
    } else {
      configure();
      make();
    }
    compileGimple();
    buildContext.getLogger().info("Compilation of GNU R sources succeeded.");
  }

  private void buildWithVagrant() throws IOException, InterruptedException {
    writeVagrantFile();
    writeVagrantBuildScript();

    int exitCode = new ProcessBuilder()
            .command("vagrant", "up")
            .directory(source.getPackageDir())
            .inheritIO()
            .start()
            .waitFor();

    if(exitCode != 0) {
      buildContext.getLogger().error("Vagrant up failed.");
    }

    exitCode = new ProcessBuilder()
            .command("vagrant", "ssh", "-c", "cd package && sh compile-native.sh")
            .directory(source.getPackageDir())
            .inheritIO()
            .start()
            .waitFor();

    if(exitCode != 0) {
      buildContext.getLogger().error("Vagrant up failed.");
    }
  }

  /**
   * Writes a VagrantFile to the package root that can be used to compile this package's
   * native sources into Gimple. This VagrantFile references the <a href="https://app.vagrantup.com/renjin/boxes/packager">pre-built VM published to
   * Vagrant Cloud</a>
   */
  private void writeVagrantFile() throws IOException {
    File vagrantFile = new File(source.getPackageDir(), "Vagrantfile");
    URL resource = Resources.getResource(NativeSourceBuilder.class, "Vagrantfile.package");

    Files.asByteSink(vagrantFile).write(Resources.toByteArray(resource));
  }

  /**
   * Writes a shell script that will run inside the Virtual Machine to compile the native sources
   * to Gimple in JSON format. The JSON files can be read by subsequent steps outside the Virtual Machine.
   */
  private void writeVagrantBuildScript() throws IOException {
    File buildScript = new File(source.getPackageDir(), "compile-native.sh");
    try(PrintWriter ps = new PrintWriter(buildScript, "UTF-8")) {
      if(!skipConfigure()) {
        ps.println("./configure");
      }
      ps.println("cd src");
      ps.println("export R_VERSION=" + RVersion());
      ps.println("export R_HOME=/usr/local/lib/renjin");
      ps.println("export R_INCLUDE_DIR=/usr/local/lib/renjin/include");
      ps.println("export R_SHARE_DIR=/usr/local/lib/renjin/share");
      ps.println("export R_PACKAGE_NAME=" + source.getPackageName());
      ps.println("export R_INSTALL_PACKAGE=" + source.getPackageName());
      ps.println("export R_PACKAGE_DIR=/home/ubuntu/package");
      ps.println("export MAKE=make");
      ps.println("export R_UNZIPCMD=/usr/bin/unzip");
      ps.println("export R_GZIPCMD=/usr/bin/gzip");
      ps.println("export CLINK_CPPFLAGS=-I/home/vagrant/package/" + unpackedIncludesDirRelativeToPackage());

      ps.println(makeCommandLine("/usr/local/lib/renjin", "/usr/local/lib/renjin/bridge.so")
              .stream()
              .map(a -> maybeQuoteShellArgument(a))
              .collect(Collectors.joining(" ")));
    }
  }

  private String unpackedIncludesDirRelativeToPackage() throws IOException {
    StringBuilder s = new StringBuilder();
    File dir = buildContext.getUnpackedIncludesDir().getCanonicalFile().getAbsoluteFile();
    File packageDir = source.getPackageDir().getCanonicalFile().getAbsoluteFile();
    while(!dir.equals(packageDir)) {
      if(s.length() > 0) {
        s.insert(0, "/");
      }
      s.insert(0, dir.getName());
      dir = dir.getParentFile();
      if(dir == null) {
        throw new IllegalStateException(String.format(
                "The unpackaged includes dir (%s) must be a sub-directory of the package directory (%s)",
                buildContext.getUnpackedIncludesDir().getAbsolutePath(),
                source.getSourceDir().getAbsolutePath()));
      }
    }
    return s.toString();
  }

  private String maybeQuoteShellArgument(String a) {
    if(a.contains("=") || a.contains(" ") || a.contains("$(")) {
      return "\"" + a + "\"";
    } else {
      return a;
    }
  }

  /**
   * Runs the configure script for the package. This is NOT run inside a
   * virtual machine.
   */
  private void configure() throws IOException, InterruptedException {

    if(skipConfigure()) {
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

  /**
   * Returns true if the configure script can be skipped, either because it doesn't exist
   * or because a specific Makevars.renjin is provided.
   */
  private boolean skipConfigure() {
    File renjinMakeVars = new File(source.getNativeSourceDir(), "Makevars.renjin");
    if (renjinMakeVars.exists()) {
      buildContext.getLogger().debug("Makevars.renjin exists, skipping ./configure...");
      return true;
    }

    File configure = new File(source.getPackageDir(), "configure");
    return !configure.exists();
  }

  private void make() throws IOException, InterruptedException {

    List<String> commandLine = makeCommandLine(buildContext.getGnuRHomeDir().getAbsolutePath(), buildContext.getGccBridgePlugin().getAbsolutePath());

    buildContext.getLogger().debug("Executing " + Joiner.on(" ").join(commandLine));

    // Setup process
    ProcessBuilder builder = new ProcessBuilder()
        .command(commandLine)
        .directory(source.getNativeSourceDir())
        .inheritIO();

    builder.environment().put("R_VERSION", RVersion());
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

  /**
   * Returns the version of R we are pretending to be.
   */
  private String RVersion() {
    return "3.5.3";
  }

  private List<String> makeCommandLine(String homeDir, String pluginPath) throws IOException {

    List<String> commandLine = Lists.newArrayList();
    commandLine.add("make");

    String makeconfFile = homeDir + "/etc/Makeconf";
    String shlibMk = homeDir + "/share/make/shlib.mk";

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
    commandLine.add(makeconfFile);

    commandLine.add("-f");
    commandLine.add(shlibMk);

    commandLine.add("SHLIB='" + source.getPackageName() + ".so'");

    if(!definedByMakeVars(makevars, "^OBJECTS\\s*=")) {
      commandLine.add("OBJECTS=" + findObjectFiles());
    }
    commandLine.add("BRIDGE_PLUGIN=" + pluginPath);

    // Packages using native code can defined the C++ standard in DESCRIPTION file
    // SystemRequirements fields or in Makevars file as CXX_STD variable. Using this
    // information, GNU R (src/library/tools/R/install.R), overwrites the flags as
    // defined in Makeconf. In Renjin, we check whether if the CXX11 flag is set in
    // DESCRIPTION or Makevars and overwrite the flags that install.R would otherwise
    // overwrite. We should add other cases from install.R ones we move to new version
    // of gcc that supports C++14 and C++17.
    if(definedByMakeVars(makevars, "(CXX_STD)\\W+(CXX11)") || source.isCXX11()) {
      if(!source.isCXX11()) {
        buildContext.getLogger().debug("Checking whether in Makevars CXX_STD is set to CXX11... yes");
      }
      commandLine.add("CXX=$(CXX11) $(CXX11STD)");
      commandLine.add("CXXFLAGS=$(CXX11FLAGS)");
      commandLine.add("CXXPICFLAGS=$(CXX11PICFLAGS)");
      commandLine.add("SHLIB_LDFLAGS=$(SHLIB_CXX11LDFLAGS)");
      commandLine.add("SHLIB_LD=$(SHLIB_CXX11LD)");
    } else {
      buildContext.getLogger().debug("Checking whether in Makevars CXX_STD is set to CXX11... no");
    }
    return commandLine;
  }

  /**
   * Reads in the Gimple JSON files generating by the make process and compiles them to
   * Java bytecode.
   */
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

    GnurSourcesCompiler.setupCompiler(compiler);

    if(transformGlobalVariables) {
      compiler.addPlugin(new GlobalVarPlugin(compiler.getPackageName()));
    }

    try {
      compiler.compile(gimpleFiles);
    } catch (Exception e) {
      throw new BuildException("Failed to compile Gimple", e);
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

    return Files.asCharSource(makevars, Charsets.UTF_8).readLines(new LineProcessor<Boolean>() {

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
