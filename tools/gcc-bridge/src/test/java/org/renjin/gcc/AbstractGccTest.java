package org.renjin.gcc;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.CallingConventions;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;

import java.io.File;
import java.lang.reflect.Method;
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


  protected Class<?> compile(String source, String className) throws Exception {
    return compile(Lists.newArrayList(source), className);
  }

  protected Class<?> compile(List<String> sources, String className) throws Exception {

    Gcc gcc = new Gcc();
    if(Strings.isNullOrEmpty(System.getProperty("gcc.bridge.plugin"))) {
      gcc.extractPlugin();
    } else {
      gcc.setPluginLibrary(new File(System.getProperty("gcc.bridge.plugin")));
    }
    gcc.setDebug(true);
    gcc.setGimpleOutputDir(new File("target/gimple"));

    List<GimpleFunction> functions = Lists.newArrayList();

    for (String sourceName : sources) {
      File source = new File(getClass().getResource(sourceName).getFile());
      GimpleCompilationUnit gimple = gcc.compileToGimple(source);
      System.out.println(gimple);

      CallingConvention callingConvention = CallingConventions.fromFile(source);
      for (GimpleFunction function : gimple.getFunctions()) {
        function.setCallingConvention(callingConvention);
      }

      functions.addAll(gimple.getFunctions());
    }

    return compileGimple(className, functions);
  }

  protected Class<?> compileGimple(String className, List<GimpleFunction> functions) throws Exception {

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setJimpleOutputDirectory(new File("target/test-jimple"));
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin.gcc");
    compiler.setClassName(className);
    compiler.setVerbose(true);
    compiler.getMethodTable().addReferenceClass(RStubs.class);
    compiler.compile(functions);

    return Class.forName("org.renjin.gcc." + className);
  }
}
