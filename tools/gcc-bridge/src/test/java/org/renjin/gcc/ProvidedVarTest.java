package org.renjin.gcc;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleCompilationUnit;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ProvidedVarTest extends AbstractGccTest {
  
  public static int jvm_field = 92;
  
  @Test
  public void test() throws Exception {

    GimpleCompilationUnit unit = compileToGimple("provided_vars.c");

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin.gcc");
    compiler.setVerbose(true);
    compiler.addVariable("jvm_field", ProvidedVarTest.class);

    compiler.compile(Collections.singletonList(unit));

    Class<?> clazz = Class.forName("org.renjin.gcc.provided_vars");
    Method test = clazz.getMethod("test");
    int result = (Integer)test.invoke(null);
    
    assertThat(result, equalTo(92));
  }
  
}
