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
package org.renjin.gcc;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.ByteStreams;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repackaged.guava.io.Resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Invokes the GCC command line compiler
 *
 */
public class Gcc {

  private File workingDirectory;
  private File pluginLibrary;

  private boolean trace = false;

  private List<File> includeDirectories = Lists.newArrayList();

  private static final Logger LOGGER = Logger.getLogger(Gcc.class.getName());

  private boolean debug;
  private File gimpleOutputDir;

  private boolean link = false;
  
  private List<String> cFlags = Lists.newArrayList();

  private List<String> cxxFlags = Lists.newArrayList();
  
  public Gcc() {
    workingDirectory = Files.createTempDir();
    gimpleOutputDir = workingDirectory;
  }
  
  public Gcc(File workingDirectory) {
    this.workingDirectory = workingDirectory;
    gimpleOutputDir = workingDirectory;
  }

  public void setPluginLibrary(File pluginLibrary) {
    this.pluginLibrary = pluginLibrary;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public void setTrace(boolean trace) {
    this.trace = trace;
  }

  public void setGimpleOutputDir(File gimpleOutputDir) {
    this.gimpleOutputDir = gimpleOutputDir;
    this.gimpleOutputDir.mkdirs();
  }

  public void addCFlags(List<String> flags) {
    cFlags.addAll(flags);
  }

  public void addCxxFlags(List<String> flags) {
    cxxFlags.addAll(flags);
  }
  
  public File getGimpleOutputDir() {
    return gimpleOutputDir;
  }

  public GimpleCompilationUnit compileToGimple(File source, String... compilerFlags) throws IOException {

    checkEnvironment();
    
    List<String> arguments = Lists.newArrayList();
    
    // Force compilation to i386 so that our pointers are 32-bits
    // rather than 64-bit. We use arrays to back pointers,
    // and java arrays can only be indexed by 32-bit integers
    arguments.add("-m32");

    // for debugging preprocessor output
//    arguments.add("-E");
//    arguments.add("-P");

    arguments.add("-D");
    arguments.add("_GCC_BRIDGE");
    arguments.add("-D");
    arguments.add("_RENJIN");

    if(!link) {
      arguments.add("-c"); // compile only, do not link
      arguments.add("-S"); // stop at assembly generation
    }
    arguments.addAll(Arrays.asList(compilerFlags));
//    arguments.add("-O9"); // highest optimization

    arguments.add("-fdump-tree-gimple-verbose-raw-vops");
    arguments.add("-save-temps");

    // Enable our plugin which dumps the Gimple as JSON
    // to standard out
    arguments.add("-fplugin=" + pluginLibrary.getAbsolutePath());

    File gimpleFile = new File(gimpleOutputDir, source.getName() + ".gimple");
    arguments.add("-fplugin-arg-bridge-json-output-file=" +
        gimpleFile.getAbsolutePath());

    for (File includeDir : includeDirectories) {
      arguments.add("-I");
      arguments.add(includeDir.getAbsolutePath());
    }
    
    if(source.getName().toLowerCase().endsWith(".c")) {
      arguments.addAll(cFlags);
    }

    if(source.getName().toLowerCase().endsWith(".cc") ||
        source.getName().toLowerCase().endsWith(".cpp")) {
      arguments.addAll(cxxFlags);
    }

    arguments.add(source.getAbsolutePath());

    LOGGER.info("Executing " + Joiner.on(" ").join(arguments));

    callGcc(arguments);
    
    GimpleParser parser = new GimpleParser();
    GimpleCompilationUnit unit = parser.parse(gimpleFile);
    unit.setSourceFile(gimpleFile);
    return unit;
  }

  public boolean isLink() {
    return link;
  }

  public void setLink(boolean link) {
    this.link = link;
  }

  private String callGcc(String... arguments) throws IOException {
    return callGcc(Arrays.asList(arguments));
  }

  /**
   * Executes GCC and returns the standard output and error
   * @param arguments
   * @return
   * @throws IOException
   * @throws GccException if the GCC process does not exit successfully
   */
  private String callGcc(List<String> arguments) throws IOException {
    List<String> command = Lists.newArrayList();
    command.add("gcc-4.7");
    command.addAll(arguments);

    System.err.println("EXECUTING: " + Joiner.on(" ").join(arguments));

    Process gcc = new ProcessBuilder().command(command).directory(workingDirectory).redirectErrorStream(true).start();

    OutputCollector outputCollector = new OutputCollector(gcc);
    Thread collectorThread = new Thread(outputCollector);
    collectorThread.start();

    try {
      gcc.waitFor();
      collectorThread.join();
    } catch (InterruptedException e) {
      throw new GccException("Compiler interrupted");
    }

    if (gcc.exitValue() != 0) {

      if(outputCollector.getOutput().contains("error trying to exec 'f951': execvp: No such file or directory")) {
        throw new GccException("Compilation failed: Fortran compiler is missing:\n" + outputCollector.getOutput());
      }

      throw new GccException("Compilation failed:\n" + outputCollector.getOutput());
    }
    return outputCollector.getOutput();
  }

  private void checkEnvironment() {
    if(PlatformUtils.OS == PlatformUtils.OSType.WINDOWS) {
      throw new GccException("Sorry, gcc-bridge does not work on Windows/Cygwin because of problems building \n" +
              "and linking the required gcc plugin. You can still compile on a *NIX platform and use the " +
              "resulting pure-Java class files on any platform.");
    }
  }

  public void addIncludeDirectory(File path) {
    includeDirectories.add(path);
  }
  
  public void extractPlugin() throws IOException {

    if(!Strings.isNullOrEmpty(System.getProperty("gcc.bridge.plugin"))) {
      pluginLibrary = new File(System.getProperty("gcc.bridge.plugin"));
      if(pluginLibrary.exists()) {
        System.err.println("Using bridge.so at " + pluginLibrary.getAbsolutePath());
        return;
      } else {
        System.err.println("bridge.so does not exist at " + pluginLibrary.getAbsolutePath());
      }
    }

    /* .so because gcc only ever looks for .so files */
    pluginLibrary = new File(workingDirectory, "bridge.so");

    compilePlugin(pluginLibrary, trace);
    
    pluginLibrary.deleteOnExit();
  }

  @Deprecated
  public synchronized static void extractPluginTo(File pluginLibrary) throws IOException {
    compilePlugin(pluginLibrary, false);
  }

  public synchronized static void compilePlugin(File pluginLibrary) throws IOException {
    compilePlugin(pluginLibrary, false);
  }


  public synchronized static void compilePlugin(File pluginLibrary, boolean trace) throws IOException {
    Preconditions.checkArgument(pluginLibrary.getName().endsWith(".so"), "plugin name must end in .so");

    if(pluginLibrary.exists()) {
      return;
    }

    if(!pluginLibrary.getParentFile().exists()) {
      boolean created = pluginLibrary.getParentFile().mkdirs();
      if(!created) {
        throw new IOException("Could not create directory " + pluginLibrary.getParentFile());
      }
    }

    File workingDir = Files.createTempDir();
    Gcc gcc = new Gcc(workingDir);

    // Extract source
    File sourceFile = new File(workingDir, "plugin.c");
    Resources.asByteSource(Resources.getResource(Gcc.class, "plugin.c")).copyTo(Files.asByteSink(sourceFile));

    // Query location of plugin headers
    String pluginDir = gcc.callGcc("-print-file-name=plugin").trim();

    // Compile and link source
    List<String> args = new ArrayList<>();
    args.add("-shared");
    args.add("-xc++");
    args.add("-I" + pluginDir + "/include");
    if(trace) {
      args.add("-DTRACE_GCC_BRIDGE");
    }
    args.add("-fPIC");
    args.add("-fno-rtti");
    args.add("-O2");
    args.add("plugin.c");
    args.add("-lstdc++");
    args.add("-shared-libgcc");
    args.addAll(Arrays.asList("-o", pluginLibrary.getAbsolutePath()));


    gcc.callGcc(args);
  }


  public void checkVersion() {
    try {
      String versionOutput = callGcc(Arrays.asList("--version"));
      if (!versionOutput.contains("4.7.4")) {
        System.err.println("WARNING: gcc-bridge has been tested against 4.7.4, other versions may not work correctly.");
      }
    } catch (IOException e) {
      throw new GccException("Failed to start GCC: " + e.getMessage() + ".\n" +
              "Make sure gcc 4.7.4 is installed." );
    }
  }



  private static class OutputCollector implements Runnable {

    private Process process;
    private String output;

    private OutputCollector(Process process) {
      this.process = process;
    }

    @Override
    public void run() {
      try {
        output = new String(ByteStreams.toByteArray(process.getInputStream()));
      } catch (IOException e) {

      }
    }

    private String getOutput() {
      return output;
    }
  }

}
