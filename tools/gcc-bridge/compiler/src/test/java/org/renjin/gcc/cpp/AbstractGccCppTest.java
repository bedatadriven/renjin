package org.renjin.gcc.cpp;

import org.renjin.gcc.AbstractGccTest;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.codegen.lib.cpp.CppSymbolLibrary;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AbstractGccCppTest extends AbstractGccTest {

  @Override 
  protected void compileGimple(List<GimpleCompilationUnit> units) throws Exception {
    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setRecordClassPrefix(units.get(0).getName());
    compiler.setPackageName(PACKAGE_NAME);
    compiler.setVerbose(true);
    compiler.addLibrary(new CppSymbolLibrary());
    compiler.compile(units);
  }

  public List<GimpleCompilationUnit> compileToGimple(List<String> sources) throws IOException {
    File workingDir = new File("target/gcc-work");
    workingDir.mkdirs();

    Gcc gcc = new Gcc(workingDir);
    if(Strings.isNullOrEmpty(System.getProperty("gcc.bridge.plugin"))) {
      gcc.extractPlugin();
    } else {
      gcc.setPluginLibrary(new File(System.getProperty("gcc.bridge.plugin")));
    }
    gcc.setDebug(true);
    gcc.setGimpleOutputDir(new File("target/gimple"));


    List<GimpleCompilationUnit> units = Lists.newArrayList();

    for (String sourceName : sources) {
      File source = new File(AbstractGccCppTest.class.getResource(sourceName).getFile());
      GimpleCompilationUnit unit = gcc.compileToGimple(source);
      units.add(unit);
    }
    return units;
  }
}