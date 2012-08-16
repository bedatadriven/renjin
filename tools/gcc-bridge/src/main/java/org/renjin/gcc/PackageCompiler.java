package org.renjin.gcc;


import com.google.common.collect.Lists;
import org.renjin.RenjinCApi;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.List;

public class PackageCompiler {

  private List<File> sources = Lists.newArrayList();

  public void addSource(File source) {
    sources.add(source);
  }

  public void compile() throws Exception {

    List<GimpleFunction> functions = Lists.newArrayList();

    Gcc gcc = new Gcc();
    gcc.addIncludeDirectory(findIncludeDir());

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
    compiler.setPackageName("org.renjin");
    compiler.setClassName("Stats");
    compiler.getMethodTable().addReferenceClass(RenjinCApi.class);
    compiler.compile(functions);
  }

  private File findIncludeDir() {
    URL url = getClass().getResource("/include/config.h");
    return new File(url.getFile()).getParentFile();
  }

  public static void main(String[] args) throws Exception {
    PackageCompiler compiler = new PackageCompiler();
    compiler.addSource(new File("src/test/resources/org/renjin/stat/distance.c"));
    compiler.addSource(new File("src/test/resources/org/renjin/stat/massdist.c"));
    compiler.addSource(new File("src/test/resources/org/renjin/stat/approx.c"));
    compiler.compile();

    Class statsClass = Class.forName("org.renjin.Stats");
    statsClass.getMethod("R_distance", DoublePtr.class, IntPtr.class, IntPtr.class,
        DoublePtr.class, IntPtr.class, IntPtr.class, DoublePtr.class);
  }

}
