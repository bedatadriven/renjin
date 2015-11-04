package org.renjin.gcc;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleCompilationUnit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ProvidedRecordTest extends AbstractGccTest {
  
  @Test
  public void test() throws Exception {
    
    GimpleCompilationUnit unit = compileToGimple("jvm_rect.c");

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin.gcc");
    compiler.setClassName("JvmRectTest");
    compiler.setVerbose(true);
    compiler.addMathLibrary();
    compiler.addRecordClass("jvm_rect", JvmRect.class);
    compiler.addMethod("jvm_area", JvmRect.class, "area");
    compiler.compile(Collections.singletonList(unit));

    Class<?> clazz = Class.forName("org.renjin.gcc.JvmRectTest");
    Method testMethod = clazz.getMethod("test");
  
    double area = (Double)testMethod.invoke(null, new JvmRect(20, 3));
  
    assertThat(area, equalTo(60d));
  
  }
  
  
}
