package org.renjin.packaging;


import com.google.common.collect.Lists;
import org.renjin.RenjinCApi;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.sexp.SEXP;

import java.io.File;
import java.io.StringReader;
import java.util.List;

public class NativeSourcesCompiler {

  private String packageName;
  private List<File> sources = Lists.newArrayList();

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  /**
   * Adds sources from 
   * @param libraryPath the package root. Native sources are expected to be in srcRoot/src
   */
  public void addSources(File src) {
    if(src.exists() && src.listFiles() != null) {
      for(File file : src.listFiles()) {
        if(file.getName().endsWith(".c") || file.getName().endsWith(".f")) {
          sources.add(file);
        }
      }
    }
  }

  public void compile() throws Exception {

    if(!sources.isEmpty()) {

      List<GimpleFunction> functions = Lists.newArrayList();

      Gcc gcc = new Gcc();
      gcc.addIncludeDirectory(unpackIncludes());

      GimpleParser parser = new GimpleParser();

      for(File sourceFile : sources) {
        String gimple = gcc.compileToGimple(sourceFile);
        System.out.println(gimple);
        
        try {
          functions.addAll(parser.parse(new StringReader(gimple)));
        } catch(Exception e) {
          throw new RuntimeException("Exception parsing gimple output of " + sourceFile, e);
        }
      }

      GimpleCompiler compiler = new GimpleCompiler();
      compiler.setOutputDirectory(new File("target/classes"));
      compiler.setPackageName("org.renjin." + packageName.toLowerCase());
      compiler.setClassName(properCase(packageName));
      compiler.getMethodTable().addReferenceClass(RenjinCApi.class);
      compiler.compile(functions);
    }
  }

  private File unpackIncludes() {
    return new File("src/main/include");
  }

  private String properCase(String packageName) {
    return packageName.substring(0,1).toUpperCase() + packageName.substring(1).toLowerCase();
  }

  public static void main(String[] args) throws Exception {
    NativeSourcesCompiler compiler = new NativeSourcesCompiler();
    compiler.setPackageName("stats");
    compiler.addSources(new File("src/library"));
    compiler.compile();
  }

}
