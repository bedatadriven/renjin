package org.renjin.gcc;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleCompilationUnit;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class TrampolineClassTest extends AbstractGccTest {

  @Test
  public void test() throws Exception {

    List<GimpleCompilationUnit> units = compileToGimple(Arrays.asList("link1.c", "link2.c"));

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin.gcc");
    compiler.setVerbose(true);
    compiler.addMathLibrary();
    compiler.addRecordClass("jvm_rect", JvmRect.class);
    compiler.addMethod("jvm_area", JvmRect.class, "area");
    compiler.setClassName("Linked");
    compiler.compile(units);
    
    Class<?> clazz = Class.forName("org.renjin.gcc.Linked");
    Method externMethod = clazz.getMethod("shared_triple", int.class);

    int result = (Integer)externMethod.invoke(null, 3);

    assertThat(result, equalTo(9));

  }


}
