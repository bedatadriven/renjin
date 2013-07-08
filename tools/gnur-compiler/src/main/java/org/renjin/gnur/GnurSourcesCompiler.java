package org.renjin.gnur;


import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GccException;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.translate.call.MallocCallTranslator;

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

public class GnurSourcesCompiler {

  private String packageName;
  private String className;
  private boolean verbose = true;
  private List<File> sources = Lists.newArrayList();
  private List<File> classPaths = Lists.newArrayList();
  private File jimpleDirectory = new File("target/jimple");
  private File gimpleDirectory = new File("target/gimple");
  private File workDirectory;
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

  public void setWorkDirectory(File workDir) {
    this.workDirectory = workDir;
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

      workDirectory.mkdirs();
      jimpleDirectory.mkdirs();
      gimpleDirectory.mkdirs();

      if(checkUpToDate()) {
        return;
      }
      
      List<GimpleCompilationUnit> units = Lists.newArrayList();

      Gcc gcc = new Gcc(getWorkDirectory());
      gcc.extractPlugin();
      gcc.addIncludeDirectory(unpackIncludes());
      gcc.setGimpleOutputDir(gimpleDirectory);

      for(File sourceFile : sources) {
        GimpleCompilationUnit unit;
        try {

          unit = gcc.compileToGimple(sourceFile, "-std=gnu99");
        } catch(Exception e) {
          throw new GccException("Error compiling " + sourceFile + " to gimple: " + e.getMessage(), e);
        }

        try {
          units.add(unit);
        } catch(Exception e) {
          throw new RuntimeException("Exception parsing unit output of " + sourceFile, e);
        }
      }
      
      File jimpleOutput = new File("target/jimple");
      jimpleOutput.mkdirs();

      GimpleCompiler compiler = new GimpleCompiler();
      compiler.setJimpleOutputDirectory(jimpleDirectory);
      compiler.setOutputDirectory(outputDirectory);
    
      compiler.setPackageName(packageName);
      compiler.setClassName(className);
      compiler.addSootClassPaths(classPaths);
      compiler.setVerbose(verbose);

      compiler.getMethodTable().addMathLibrary();

      compiler.getMethodTable().addCallTranslator(new RallocTranslator());

      compiler.getMethodTable().addReferenceClass(Class.forName("org.renjin.appl.Appl"));

      compiler.getMethodTable().addReferenceClass(RenjinCApi.class);
      compiler.getMethodTable().addReferenceClass(Sort.class);

      compiler.compile(units);
    }
  }

  private boolean checkUpToDate() {
    if(sourcesLastModified() < classLastModified()) {
      System.out.println(packageName + "." + className + "  is up to date, skipping GNU R sources compilation");
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
    
    URL url = Resources.getResource("org/renjin/gnur/include/R.h");
    if(url.getProtocol().equals("file")) {
        return new File(url.getFile()).getParentFile();
    } else if(url.getProtocol().equals("jar")) {
      
      // file = file:/C:/Users/Alex/.m2/repository/org/renjin/renjin-gnur-compiler/0.7.0-SNAPSHOT/renjin-gnur-compiler-0.7.0-SNAPSHOT.jar!/org/renjin/gnur/include/R.h
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
    File tempDir = getWorkDirectory();
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

  private File getWorkDirectory() {
    if(workDirectory == null) {
      workDirectory = Files.createTempDir();
    }
    return workDirectory;
  }

}
