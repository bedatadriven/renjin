/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gnur;


import org.apache.commons.math.special.Erf;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GccException;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.HtmlTreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gnur.api.*;
import org.renjin.gnur.api.Error;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.sexp.SEXP;

import java.io.File;
import java.util.Date;
import java.util.List;

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
  private File loggingDir;

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

  public void setLoggingDir(File loggingDir) {
    this.loggingDir = loggingDir;
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
    return name.endsWith(".c") || name.endsWith(".cpp") ||
            name.toLowerCase().endsWith(".f") || name.toLowerCase().endsWith(".f90") ||
            name.toLowerCase().endsWith(".f95") || name.toLowerCase().endsWith(".f03") ||
            name.toLowerCase().endsWith(".for");
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

      File gnurHomeDir = GnurInstallation.unpackRHome(Files.createTempDir());
      
      List<GimpleCompilationUnit> units = Lists.newArrayList();

      Gcc gcc = new Gcc(getWorkDirectory());
      gcc.extractPlugin();
      gcc.addIncludeDirectory(new File(gnurHomeDir, "include"));
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

      compiler.setLinkClassLoader(linkClassLoader);
      compiler.addMathLibrary();

      setupCompiler(compiler);

      if(loggingDir != null) {
        compiler.setLogger(new HtmlTreeLogger(loggingDir));
      }

      compiler.compile(units);
    }
  }

  public static void setupCompiler(GimpleCompiler compiler) throws ClassNotFoundException {
    compiler.addReferenceClass(Class.forName("org.renjin.appl.Appl"));
    compiler.addReferenceClass(Class.forName("org.renjin.math.Blas"));
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
    compiler.addReferenceClass(Internal.class);
    compiler.addReferenceClass(Memory.class);
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
    compiler.addReferenceClass(RStartup.class);
    compiler.addReferenceClass(S.class);
    compiler.addReferenceClass(Startup.class);
    compiler.addReferenceClass(stats_package.class);
    compiler.addReferenceClass(stats_stubs.class);
    compiler.addReferenceClass(Utils.class);

    compiler.addRecordClass("SEXPREC", SEXP.class);

    compiler.addRecordClass("_GESystemDesc", GESystemDesc.class);
    compiler.addRecordClass("_GEDevDesc", GEDevDesc.class);
    compiler.addRecordClass("_DevDesc", DevDesc.class);
    compiler.addRecordClass("_R_GE_gcontext", GEContext.class);

    compiler.addReferenceClass(Rdynload.class);
    compiler.addRecordClass("_DllInfo", DllInfo.class);
    compiler.addReferenceClass(RenjinFiles.class);
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
