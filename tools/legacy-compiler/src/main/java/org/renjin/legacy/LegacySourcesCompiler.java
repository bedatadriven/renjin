package org.renjin.legacy;


import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GccException;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LegacySourcesCompiler {

  private String packageName;
  private String className;
  private boolean verbose = true;
  private List<File> sources = Lists.newArrayList();
  private List<File> classPaths = Lists.newArrayList();
  private File jimpleDirectory = new File("target/jimple");
  private File gimpleDirectory = new File("target/gimple");

  private File outputDirectory = new File("target/classes");

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
  
  public void setClassName(String className) {
    this.className = className;
  }
 
  public void setJimpleDirectory(File jimpleOutputDirectory) {
    this.jimpleDirectory = jimpleOutputDirectory;
  }

  public void setGimpleDirectory(File gimpleDirectory) {
    this.gimpleDirectory = gimpleDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void addSources(File src) {
    if(src.exists() && src.listFiles() != null) {
      for(File file : src.listFiles()) {
        if(file.getName().endsWith(".c") || file.getName().endsWith(".f")) {
          sources.add(file);
        }
      }
    }
  }

  public void addClassPaths(List<File> paths) {
    classPaths.addAll(paths);
  }


  public void compile() throws Exception {

    if(!sources.isEmpty()) {

      if(checkUpToDate()) {
        return;
      }
      
      List<GimpleFunction> functions = Lists.newArrayList();

      Gcc gcc = new Gcc();
      gcc.extractPlugin();
      gcc.addIncludeDirectory(unpackIncludes());

      for(File sourceFile : sources) {
        GimpleCompilationUnit gimple;
        try {
        gimple = gcc.compileToGimple(sourceFile);
        } catch(Exception e) {
          throw new GccException("Error compiling " + sourceFile + "to gimple: " + e.getMessage(), e);
        }

        try {
          functions.addAll(gimple.getFunctions());
        } catch(Exception e) {
          throw new RuntimeException("Exception parsing gimple output of " + sourceFile, e);
        }
      }
      
      File jimpleOutput = new File("target/jimple");
      jimpleOutput.mkdirs();

      GimpleCompiler compiler = new GimpleCompiler();
      compiler.setJimpleOutputDirectory(jimpleDirectory);
      compiler.setGimpleOutputDirectory(gimpleDirectory);
      compiler.setOutputDirectory(outputDirectory);
      compiler.setPackageName(packageName);
      compiler.setClassName(className);
      compiler.addSootClassPaths(classPaths);
      compiler.setVerbose(verbose);
      compiler.getMethodTable().addReferenceClass(RenjinCApi.class);
      compiler.compile(functions);
    }
  }

  private boolean checkUpToDate() {
    if(sourcesLastModified() < classLastModified()) {
      System.out.println(packageName + "." + className + "  is up to date, skipping legacy compilation");
      return true;
    } else { 
      return false;
    }
  }

  private long classLastModified() {
    File classFile = new File(outputDirectory.getAbsolutePath() + File.separator +
        packageName.replace('.', File.separatorChar) +
        File.separator + className + ".class");
    System.out.println("class file (" + classFile.getAbsolutePath() + ") last modified on " + new Date(classFile.lastModified()));
    return classFile.lastModified();
  }

  private long sourcesLastModified() {
    long lastModified = 0;
    for(File source: sources) {
      if(source.lastModified() > lastModified) {
        lastModified = source.lastModified();
      }
    }
    System.out.println("sources last modified: " + lastModified);

    return lastModified;
  }

  private File unpackIncludes() throws IOException {
    
    URL url = Resources.getResource("org/renjin/legacy/include/R.h");
    if(url.getProtocol().equals("file")) {
        return new File(url.getFile()).getParentFile();
    } else if(url.getProtocol().equals("jar")){
      
      // file = file:/C:/Users/Alex/.m2/repository/org/renjin/renjin-legacy-compiler/0.7.0-SNAPSHOT/renjin-legacy-compiler-0.7.0-SNAPSHOT.jar!/org/renjin/legacy/include/R.h
      if(url.getFile().startsWith("file:")) {
          
        int fileStart = url.getFile().indexOf("!");
        String jarPath = url.getFile().substring("file:".length(), fileStart);
        String includePath = url.getFile().substring(fileStart+1+"/".length());
        includePath = includePath.substring(0, includePath.length() - "R.h".length());

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
