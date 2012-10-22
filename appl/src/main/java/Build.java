import com.google.common.collect.Lists;
import org.renjin.appl.ExternalRoutines;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;

import java.io.File;
import java.io.StringReader;
import java.util.List;

public class Build {

  public static void main(String[] args) throws Exception {

    Gcc gcc = new Gcc();
    GimpleParser parser = new GimpleParser();
    List<GimpleFunction> functions = Lists.newArrayList();

    for(File source : findSources()) {
      String gimple = gcc.compileToGimple(source);
      System.out.println(gimple);
      functions.addAll(parser.parse(new StringReader(gimple)));
    }

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin");
    compiler.setClassName("Appl");
    compiler.getMethodTable().addReferenceClass(ExternalRoutines.class);
    compiler.setVerbose(true);
    compiler.compile(functions);
  }

  private static List<File> findSources() {
    List<File> sources = Lists.newArrayList();
    File fortranRoot = new File("src/main/fortran");
    for(File source : fortranRoot.listFiles()) {
      if(source.getName().endsWith(".f")) {
        sources.add(source);
      }
    }
    return sources;
  }

}