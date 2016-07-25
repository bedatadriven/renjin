package org.renjin.gcc;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

  private List<File> includeDirectories = Lists.newArrayList();

  private static final Logger LOGGER = Logger.getLogger(Gcc.class.getName());

  private boolean debug;
  private File gimpleOutputDir;
  
  private List<String> cFlags = Lists.newArrayList();
  
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

  public void setGimpleOutputDir(File gimpleOutputDir) {
    this.gimpleOutputDir = gimpleOutputDir;
    this.gimpleOutputDir.mkdirs();
  }

  public void addCFlags(List<String> flags) {
    cFlags.addAll(flags);
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
    
  //  arguments.add("-fno-rtti");

    arguments.add("-D");
    arguments.add("_GCC_BRIDGE");
    arguments.add("-D");
    arguments.add("_RENJIN");
    arguments.add("-c"); // compile only, do not link
    arguments.add("-S"); // stop at assembly generation
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

    arguments.add(source.getAbsolutePath());

    LOGGER.fine("Executing " + Joiner.on(" ").join(arguments));

    callGcc(arguments);
    
    GimpleParser parser = new GimpleParser();
    GimpleCompilationUnit unit = parser.parse(gimpleFile);
    unit.setSourceFile(gimpleFile);
    return unit;
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
    command.add("gcc-4.6");
    command.addAll(arguments);

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

    extractPluginTo(pluginLibrary);
    
    pluginLibrary.deleteOnExit();
  }

  public static void extractPluginTo(File pluginLibrary) throws IOException {
    Preconditions.checkArgument(pluginLibrary.getName().endsWith(".so"), "plugin name must end in .so");

    String libraryName = PlatformUtils.getPortableLibraryName("gcc-bridge");
    
    URL pluginResource;
    try {
      pluginResource = Resources.getResource("org/renjin/gcc/" + libraryName);
    } catch(IllegalArgumentException e) {
      throw new GccException("Could not find a bundled version of the gcc plugin for your platform.\n" +
              "(Was expecting: /org/renjin/gcc/" +  libraryName + " on the classpath.)\n" +
              "You will need to build it yourself and specify the path to the binary. ");
    }

    Resources.asByteSource(pluginResource).copyTo(Files.asByteSink(pluginLibrary));
  }

  public void checkVersion() {
    try {
      String versionOutput = callGcc(Arrays.asList("--version"));
      if (!versionOutput.contains("4.6.3")) {
        System.err.println("WARNING: gcc-bridge has been tested against 4.6.3, other versions may not work correctly.");
      }
    } catch (IOException e) {
      throw new GccException("Failed to start GCC: " + e.getMessage() + ".\n" +
              "Make sure gcc 4.6.3 is installed." );
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
