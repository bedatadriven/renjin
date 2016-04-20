package org.renjin.gcc.cpp;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

public class CppClassTest extends AbstractGccCppTest {

  @Test
  public void createClass() throws Exception {
    compile("cppclass.cpp");

    Class<?> clazz = Class.forName("org.renjin.gcc.cppclass");
    Method method = clazz.getMethod("create");

    Object rect = method.invoke(null);
    assertThat(rect, is(not(nullValue())));
  }

  @Test
  public void constructors() throws Exception {
    Class<?> clazz = compile("constructors.cpp");

    Method method = clazz.getMethod("run");
    Integer retval = (Integer) method.invoke(null);
    assertThat(retval, is(Integer.valueOf(5)));
  }

  @Test
  public void destructors() throws Exception {
    Class<?> clazz = compile("destructors.cpp");

    Method method = clazz.getMethod("run");
    Integer retval = (Integer) method.invoke(null);
    assertThat(retval, is(Integer.valueOf(3)));
  }
}