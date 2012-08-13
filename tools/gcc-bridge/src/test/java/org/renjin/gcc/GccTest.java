package org.renjin.gcc;

import org.junit.Test;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.gcc.runtime.DoubleArrayPointer;
import org.renjin.gcc.runtime.Pointer;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class GccTest {

	@Test
	public void simpleTest() throws Exception {

    Class clazz = compile("area.c", "Area");
		
		// try to load class
		Method method = clazz.getMethod("circle_area", double.class);

    Double value = (Double) method.invoke(null, 2d);

    assertThat(value, closeTo(12.56, 0.01));
	}

  @Test
  public void pointers() throws Exception {
    Class clazz = compile("pointers.c", "Pointers");

    Method method = clazz.getMethod("sum_array", Pointer.class, int.class);
    Double result = (Double)method.invoke(null, new DoubleArrayPointer(15, 20, 300), 3);

    assertThat(result, equalTo(335d));

  }

  private Class<?> compile(String sourceFile, String className) throws Exception {
    File source = new File(getClass().getResource(sourceFile).getFile());

    Gcc gcc = new Gcc();
    String gimple = gcc.compileToGimple(source);

    System.out.println(gimple);

    GimpleParser parser = new GimpleParser();
    List<GimpleFunction> functions = parser.parse(new StringReader(gimple));

    System.out.println(functions);

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setPackageName("org.renjin.gcc");
    compiler.setClassName(className);
    compiler.compile(functions);

    return Class.forName("org.renjin.gcc." + className);
  }

}
