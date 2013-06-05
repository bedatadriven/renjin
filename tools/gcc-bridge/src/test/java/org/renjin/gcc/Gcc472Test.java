package org.renjin.gcc;


import org.junit.Test;
import org.renjin.gcc.gimple.F77CallingConvention;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class Gcc472Test extends Dqrdc2Test {

  @Test
  public void fortranArray() throws Exception {

    GimpleParser parser = new GimpleParser();
    GimpleCompilationUnit unit = parser.parse(getClass().getResource("2darray.f.gcc.4.7.2.gimple"));
    for(GimpleFunction fn : unit.getFunctions()) {
      fn.setCallingConvention(new F77CallingConvention());
    }

    Class clazz = compileGimple("org.renjin.gcc.ArrayTestGcc472", unit.getFunctions());


    Method method = clazz.getMethod("test", DoublePtr.class, IntPtr.class);

    double[] x = new double[9];

    method.invoke(null, new DoublePtr(x, 0), new IntPtr(3));

    System.out.println(x);

    assertThat(x[0], equalTo(1d));
    assertThat(x[4], equalTo(4d));
    assertThat(x[8], equalTo(9d));

    DoublePtr y = new DoublePtr(0);
    method = clazz.getMethod("localarray", DoublePtr.class);
    method.invoke(null, y);

    assertThat(y.unwrap(), equalTo(110d));

  }

}
