package org.renjin.gcc;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleCompilationUnit;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ProvidedRecordTest extends AbstractGccTest {
  
  @Test
  public void test() throws Exception {
    
    GimpleCompilationUnit unit = compileToGimple("provided_records.c");

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin.gcc");
    compiler.setVerbose(true);
    compiler.addMathLibrary();
    compiler.addRecordClass("jvm_rect", JvmRect.class);
    compiler.addMethod("jvm_area", JvmRect.class, "area");
    compiler.addMethod("jvm_areas", JvmRect.class, "areas");

    compiler.compile(Collections.singletonList(unit));

    Class<?> clazz = Class.forName("org.renjin.gcc.provided_records");
    Method test = clazz.getMethod("test", JvmRect.class);
  
    int area = (Integer)test.invoke(null, new JvmRect(20, 3));
    assertThat(area, equalTo(60));

    Method testMultiple = clazz.getMethod("test_multiple");
    
    int areas = (Integer)testMultiple.invoke(null);
    assertThat(areas, equalTo(850));
    
    Method testGlobals = clazz.getMethod("test_globals");
    areas = (Integer)testGlobals.invoke(null);

    assertThat(areas, equalTo( (2*4) + (3*5) + (6*8) + (10*10)));
  }
  
  
}
