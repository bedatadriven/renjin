package org.renjin.gnur;


import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.math.special.Erf;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GccException;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gnur.api.*;
import org.renjin.gnur.api.Error;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.sexp.SEXP;

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
  private File gimpleDirectory = new File("target/gimple");
  private File workDirectory;
  private File outputDirectory = new File("target/classes");
  private List<File> includeDirs = Lists.newArrayList();
  private ClassLoader linkClassLoader = getClass().getClassLoader();
  

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
  
  public void setClassName(String className) {
    this.className = className;
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
        if(isSourceFile(file.getName())) {
          sources.add(file);
        }
      }
    }
  }

  private boolean isSourceFile(String name) {
    return name.endsWith(".c") || name.endsWith(".f") || name.endsWith(".cpp");
  }

  public void setLinkClassLoader(ClassLoader linkClassLoader) {
    this.linkClassLoader = linkClassLoader;
  }

  public void compile() throws Exception {

    if(!sources.isEmpty()) {

      workDirectory.mkdirs();
      gimpleDirectory.mkdirs();

      if(checkUpToDate()) {
        return;
      }
      
      List<GimpleCompilationUnit> units = Lists.newArrayList();

      Gcc gcc = new Gcc(getWorkDirectory());
      gcc.extractPlugin();
      gcc.addIncludeDirectory(unpackIncludes());
      for (File includeDir : includeDirs) {
        gcc.addIncludeDirectory(includeDir);
      }
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
      compiler.setOutputDirectory(outputDirectory);
    
      compiler.setPackageName(packageName);
      compiler.setClassName(className);
      compiler.setVerbose(verbose);
      compiler.setLoggingDirectory(workDirectory);

      compiler.setLinkClassLoader(linkClassLoader);
      compiler.addMathLibrary();

      compiler.addReferenceClass(Class.forName("org.renjin.appl.Appl"));
      compiler.addReferenceClass(Class.forName("org.renjin.math.Blas"));
      compiler.addReferenceClass(Lapack.class);
      Class distributionsClass = Class.forName("org.renjin.stats.internals.Distributions");
      compiler.addReferenceClass(distributionsClass);
      compiler.addMethod("Rf_dbeta", distributionsClass, "dbeta");
      compiler.addMethod("Rf_pbeta", distributionsClass, "pbeta");
      compiler.addMethod("erf", Erf.class, "erf");
      compiler.addMethod("erfc", Erf.class, "erfc");
      compiler.addReferenceClass(Arith.class);
      compiler.addReferenceClass(Callbacks.class);
      compiler.addReferenceClass(Defn.class);
      compiler.addReferenceClass(Error.class);
      compiler.addReferenceClass(eventloop.class);
      compiler.addReferenceClass(Fileio.class);
      compiler.addReferenceClass(GetText.class);
      compiler.addReferenceClass(GetX11Image.class);
      compiler.addReferenceClass(Graphics.class);
      compiler.addReferenceClass(GraphicsBase.class);
      compiler.addReferenceClass(GraphicsEngine.class);
      compiler.addReferenceClass(Internal.class);
      compiler.addReferenceClass(Memory.class);
      compiler.addReferenceClass(MethodDef.class);
      compiler.addReferenceClass(Parse.class);
      compiler.addReferenceClass(Print.class);
      compiler.addReferenceClass(PrtUtil.class);
      compiler.addReferenceClass(QuartzDevice.class);
      compiler.addReferenceClass(R.class);
      compiler.addReferenceClass(R_ftp_http.class);
      compiler.addReferenceClass(Sort.class);
      compiler.addReferenceClass(Random.class);
      compiler.addReferenceClass(Rconnections.class);
      compiler.addReferenceClass(Rdynload.class);
      compiler.addReferenceClass(RenjinDebug.class);
      compiler.addReferenceClass(Rgraphics.class);
      compiler.addReferenceClass(Riconv.class);
      compiler.addReferenceClass(Rinterface.class);
      compiler.addReferenceClass(Rinternals.class);
      compiler.addReferenceClass(rlocale.class);
      compiler.addReferenceClass(Rmath.class);
      compiler.addReferenceClass(RS.class);
      compiler.addReferenceClass(RStartup.class);
      compiler.addReferenceClass(S.class);
      compiler.addReferenceClass(Startup.class);
      compiler.addReferenceClass(stats_package.class);
      compiler.addReferenceClass(stats_stubs.class);
      compiler.addReferenceClass(Utils.class);
      
      compiler.addRecordClass("SEXPREC", SEXP.class);
      
      compiler.addReferenceClass(Rdynload.class);
      compiler.addRecordClass("_DllInfo", DllInfo.class);
      compiler.addRecordClass("__MethodDef", MethodDef.class);
      
      
      
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

  public void addIncludeDir(File includeDirectory) {
    includeDirs.add(includeDirectory);
  }
}
