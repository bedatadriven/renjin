package org.renjin.packaging;


import com.google.common.collect.Lists;
import org.renjin.RenjinCApi;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;

import java.io.File;
import java.io.StringReader;
import java.util.List;

public class NativeSourcesCompiler {

  private String packageName;
  private File srcRoot;

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setSourceRoot(File srcRoot) {
    this.srcRoot = srcRoot;
  }

  private List<File> findSources() {
    List<File> sources = Lists.newArrayList();
    File packageRoot = new File(srcRoot, packageName);
    File src = new File(packageRoot, "src");

    if(src.exists() && src.listFiles() != null) {
      for(File file : src.listFiles()) {
        if(file.getName().endsWith(".c")) {
          sources.add(file);
        }
      }
    }
    return sources;
  }

  public void compile() throws Exception {

    List<File> sources = findSources();
    if(!sources.isEmpty()) {

      List<GimpleFunction> functions = Lists.newArrayList();

      Gcc gcc = new Gcc();
      gcc.addIncludeDirectory(new File("src/main/include"));

      GimpleParser parser = new GimpleParser();

      for(File sourceFile : sources) {
        String gimple = gcc.compileToGimple(sourceFile);
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

  private String properCase(String packageName) {
    return packageName.substring(0,1).toUpperCase() + packageName.substring(1).toLowerCase();
  }

  public static void main(String[] args) throws Exception {
    NativeSourcesCompiler compiler = new NativeSourcesCompiler();
    compiler.setPackageName("stats");
    compiler.setSourceRoot(new File("src/library"));
    compiler.compile();
  }

}
