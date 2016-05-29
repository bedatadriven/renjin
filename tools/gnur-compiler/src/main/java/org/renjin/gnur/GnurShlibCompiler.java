package org.renjin.gnur;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Compiles a shared library intended for GNU R to JVM classfiles
 */
public class GnurShlibCompiler {
  
  private static final List<String> SOURCE_EXTENSIONS = Lists.newArrayList("c", "f", "f77", "cpp", "cxx");
  
  private File sourceRoot;
  private File homeDir;
  private File pluginLibrary;
  
  private File outputDirectory;
  
  public static void main(String[] args) throws IOException, InterruptedException {
    
    GnurShlibCompiler compiler = new GnurShlibCompiler();
    compiler.setSourceRoot(new File("/home/alex/dev/cran/ifultools/src"));
    compiler.execute();
  }

  public GnurShlibCompiler() {
    this.sourceRoot = sourceRoot;
  }

  public File getSourceRoot() {
    return sourceRoot;
  }

  public void setSourceRoot(File sourceRoot) {
    this.sourceRoot = sourceRoot;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void execute() throws IOException, InterruptedException {
    homeDir = setupHomeDir();
    pluginLibrary = extractPluginLibrary();

    executeMake();
    compileGimple();
  }


  private File extractPluginLibrary() throws IOException {
    File tempDir = Files.createTempDir();
    File pluginFile = new File(tempDir, "bridge.so");
    Gcc.extractPluginTo(pluginFile);

    return pluginFile;
  }

  public void executeMake() throws IOException, InterruptedException {
    
    List<String> commandLine = Lists.newArrayList();
    commandLine.add("make");

    // Combine R's default Makefile with package-specific Makevars if present
    File makevars = new File(sourceRoot, "Makevars");
    if (makevars.exists()) {
      commandLine.add("-f");
      commandLine.add(makevars.getName());
    }

    // Makeconf file
    commandLine.add("-f");
    commandLine.add(homeDir.getAbsolutePath() + "/etc/Makeconf");
    
    // shlib.mk
    commandLine.add("-f");
    commandLine.add(homeDir.getAbsolutePath() + "/shlib.mk");
    
    commandLine.add("SHLIB='Matrix.so'");
    commandLine.add("OBJECTS=" + findObjectFiles());
    commandLine.add("R_INCLUDE_DIR=" + homeDir.getAbsolutePath() + "/include");
    commandLine.add("BRIDGE_PLUGIN=" + pluginLibrary.getAbsolutePath());

    System.err.println(Joiner.on(" ").join(commandLine));

    // Execute...
    int exitCode = new ProcessBuilder()
        .command(commandLine)
        .directory(sourceRoot)
        .inheritIO().start().waitFor();
    if (exitCode != 0) {
      throw new InternalCompilerException("Failed to execute Makefile");
    }
    
    System.out.println("Make completed successfully!");
  }


  private void compileGimple() {
    List<File> gimpleFiles = Lists.newArrayList();
    collectGimple(sourceRoot, gimpleFiles);
    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(outputDirectory);
//
//    compiler.setPackageName(packageName);
//    compiler.setClassName(className);
//    compiler.setVerbose(verbose);
//    compiler.setLoggingDirectory(workDirectory);

  }

  private String findObjectFiles() {
    List<String> objectFiles = new ArrayList<>();
    File[] files = sourceRoot.listFiles();
    if(files != null)  {
      for (File file : files) {
        String extension = Files.getFileExtension(file.getName());
        if(SOURCE_EXTENSIONS.contains(extension)) {
          String baseName = Files.getNameWithoutExtension(file.getName());
          objectFiles.add(baseName + ".o");
        }
      }
    }
    return Joiner.on(' ').join(objectFiles);
  }

  private void collectGimple(File dir, List<File> gimpleFiles) {
    File[] files = dir.listFiles();
    if(files != null) {
      for (File file : files) {
        if(file.getName().endsWith(".gimple")) {
          gimpleFiles.add(file);
        } else if(file.isDirectory()) {
          collectGimple(file, gimpleFiles);
        }
      }
    }
  }

  private File setupHomeDir() throws IOException {

    URL url = Resources.getResource("org/renjin/gnur/include/R.h");
    if(url.getProtocol().equals("file")) {
      return new File(url.getFile()).getParentFile().getParentFile();
    } else if(url.getProtocol().equals("jar")) {

      // file = file:/C:/Users/Alex/.m2/repository/org/renjin/renjin-gnur-compiler/0.7.0-SNAPSHOT/renjin-gnur-compiler-0.7.0-SNAPSHOT.jar!/org/renjin/gnur/include/R.h
      if(url.getFile().startsWith("file:")) {

        int fileStart = url.getFile().indexOf("!");
        String jarPath = url.getFile().substring("file:".length(), fileStart);
        String includePath = url.getFile().substring(fileStart+1+"/".length());
        includePath = includePath.substring(0, includePath.length() - "include/R.h".length());

        return extractToTemp(jarPath, includePath);
      }
    }
    throw new RuntimeException("Don't know how to unpack resources at "  + url);
  }
  
  private File extractToTemp(String jarPath, String includePath) throws IOException {
    File tempDir = Files.createTempDir();
    JarFile jar = new JarFile(jarPath);
    Enumeration<JarEntry> entries = jar.entries();
    while(entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if(entry.getName().startsWith(includePath) && !entry.isDirectory()) {
        File target = new File(tempDir.getAbsolutePath() + File.separator +
            entry.getName().substring(includePath.length()).replace('/', File.separatorChar));
        target.getParentFile().mkdirs();

        //System.err.println("extracting to "  + target);

        InputStream in = jar.getInputStream(entry);
        FileOutputStream out = new FileOutputStream(target);
        ByteStreams.copy(in, out);
        out.close();
      }
    }
    return tempDir;
  }


}
