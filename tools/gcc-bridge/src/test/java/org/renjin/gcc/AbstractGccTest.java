package org.renjin.gcc;


import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.CallingConventions;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractGccTest {

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


  protected Class<?> compile(String source, String className) throws Exception {
    return compile(Lists.newArrayList(source), className);
  }

  protected Class<?> compile(List<String> sources, String className) throws Exception {

    List<GimpleCompilationUnit> units = compileToGimple(sources);
    
    System.out.println(units);

    return compileGimple(className, units);
  }
  
  public GimpleCompilationUnit compileToGimple(String source) throws IOException {
    List<GimpleCompilationUnit> units = compileToGimple(Arrays.asList(source));
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

  protected Class<?> compileGimple(String className, List<GimpleCompilationUnit> units) throws Exception {

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));          
    compiler.setPackageName("org.renjin.gcc");
    compiler.setClassName(className);
    compiler.setVerbose(true);
    compiler.addReferenceClass(RStubs.class);
    compiler.addMathLibrary();
    compiler.compile(units);

    return Class.forName("org.renjin.gcc." + className);
  }
}
