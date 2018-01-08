/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc;


import org.junit.Before;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
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

  protected Long call(Class clazz, String methodName, long x) throws Exception {
    Method method = clazz.getMethod(methodName, long.class);
    return (Long) method.invoke(null, x);
  }

  protected Integer call(Class clazz, String methodName, byte x, byte y) throws Exception {
    Method method = clazz.getMethod(methodName, byte.class, byte.class);
    return (Integer) method.invoke(null, x, y);
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
  
  protected final Class<?> compileAndTest(String source) throws Exception {
    Class<?> clazz = compile(source);

    boolean testsRun = false;
    List<String> methods = new ArrayList<>();
    
    for (Method method : clazz.getMethods()) {
      if(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        methods.add(method.getName());
        if(method.getName().startsWith("test")) {
          method.invoke(null);
          testsRun = true;
        }
      }
    }
    
    if(!testsRun) {
      throw new IllegalStateException("No test_ methods declared: " + methods);
    }

    return clazz;
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
    gcc.addIncludeDirectory(new File("/usr/local/include/csmith-2.3.0"));

    if(Strings.isNullOrEmpty(System.getProperty("gcc.bridge.plugin"))) {
      gcc.extractPlugin();
    } else {
      gcc.setPluginLibrary(new File(System.getProperty("gcc.bridge.plugin")));
    }
    gcc.setDebug(true);
    gcc.setGimpleOutputDir(new File("target/gimple"));

    List<GimpleCompilationUnit> units = Lists.newArrayList();

    for (String sourceName : sources) {
      URL resource = AbstractGccTest.class.getResource(sourceName);
      if(resource == null) {
        throw new IOException("Could not find source: " + sourceName);
      }
      File source = new File(resource.getFile());
      GimpleCompilationUnit unit = gcc.compileToGimple(source);

      units.add(unit);
    }
    return units;
  }

  protected void compileGimple(List<GimpleCompilationUnit> units) throws Exception {

    File outputDir = new File("target/test-classes");

    // Ensure we have the correct output directory
    File expectedClassFile = new File(outputDir.getAbsolutePath() + "/" +
          getClass().getName().replace('.', '/') + ".class");
    if(!expectedClassFile.exists()) {
      throw new RuntimeException("Expected working directory to be $renjin/tools/gcc-bridge/compiler, but was: " +
        new File(".").getAbsolutePath());
    }

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(outputDir);          
    compiler.setLogger(new HtmlTreeLogger(new File("target/gcc-bridge-logs")));
    compiler.setRecordClassPrefix(units.get(0).getName());
    compiler.setPackageName(PACKAGE_NAME);
    compiler.setVerbose(true);
    compiler.addReferenceClass(RStubs.class);
    compiler.addReferenceClass(GimpleAssert.class);
    compiler.addMathLibrary();
    compiler.compile(units);
  }
}
