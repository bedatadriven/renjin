package org.renjin.gcc;


import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.junit.Before;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.CallingConventions;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public abstract class AbstractGccTest {

  @Before
  public void turnOnTracing() {
    GimpleCompiler.TRACE = true;
  }
  
  public static final String PACKAGE_NAME = "org.renjin.gcc";

  protected Integer call(Class clazz, String methodName, double x) throws Exception {
    Method method = clazz.getMethod(methodName, double.class);
    return (Integer) method.invoke(null, x);
  }

  protected int call(Class clazz, String methodName, double x, double y) throws Exception {
    Method method = clazz.getMethod(methodName, double.class, double.class);
    return (Integer) method.invoke(null, x, y);
  }

  protected int call(Class clazz, String methodName, float x, float y) throws Exception {
    Method method = clazz.getMethod(methodName, float.class, float.class);
    return (Integer) method.invoke(null, x, y);
  }


  protected int call(Class clazz, String methodName, int x, int y) throws Exception {
    Method method = clazz.getMethod(methodName, int.class, int.class);
    return (Integer) method.invoke(null, x, y);
  }

  protected int call(Class clazz, String methodName, int x) throws Exception {
    Method method = clazz.getMethod(methodName, int.class);
    return (Integer) method.invoke(null, x);
  }
  
  protected Method findMethod(Class<?> clazz, String name) {
    for (Method method : clazz.getMethods()) {
      if(method.getName().equals(name)) {
        return method;
      }
    }
    throw new IllegalArgumentException(name);
  }


  /**
   * Compiles a single source file and loads the resulting class file
   */
  protected final Class<?> compile(String source) throws Exception {
    List<GimpleCompilationUnit> units = compileToGimple(Lists.newArrayList(source));
    compileGimple(units);
    
    String className = Files.getNameWithoutExtension(source);
    
    return Class.forName(PACKAGE_NAME + "." + className);
  }

  protected void compile(List<String> sources) throws Exception {
    List<GimpleCompilationUnit> units = compileToGimple(sources);
    compileGimple(units);
  }
  
  public GimpleCompilationUnit compileToGimple(String source) throws IOException {
    List<GimpleCompilationUnit> units = compileToGimple(Collections.singletonList(source));
    return Iterables.getOnlyElement(units);
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
      File source = new File(AbstractGccTest.class.getResource(sourceName).getFile());
      GimpleCompilationUnit unit = gcc.compileToGimple(source);

      CallingConvention callingConvention = CallingConventions.fromFile(source);
      for (GimpleFunction function : unit.getFunctions()) {
        function.setCallingConvention(callingConvention);
      }
      units.add(unit);
    }
    return units;
  }

  protected void compileGimple(List<GimpleCompilationUnit> units) throws Exception {
    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));          
    compiler.setPackageName(PACKAGE_NAME);
    compiler.setVerbose(true);
    compiler.addReferenceClass(RStubs.class);
    compiler.addMathLibrary();
    compiler.compile(units);
  }
}
