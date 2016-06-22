package org.renjin.appl;

import org.junit.Test;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

public class DqrslTest {

  @Test
  public void lm() throws Exception {

    DoublePtr x = new DoublePtr(
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    IntPtr n = new IntPtr(20);
    IntPtr p = new IntPtr(2);
    DoublePtr y = new DoublePtr(
        4.17,5.58,5.18,6.11,4.5,4.61,5.17,4.53,5.33,5.14,4.81,4.17,4.41,3.59,5.87,3.83,6.03,4.89,4.32,4.69 );

    IntPtr ny = new IntPtr(1);
    DoublePtr tol = new DoublePtr(1e-7);
    DoublePtr coefficients = new DoublePtr(0, 0);
    DoublePtr residuals = new DoublePtr(
        4.17,5.58,5.18,6.11,4.5,4.61,5.17,4.53,5.33,5.14,4.81,4.17,4.41,3.59,5.87,3.83,6.03,4.89,4.32,4.69 );
    DoublePtr effects = new DoublePtr(
        4.17,5.58,5.18,6.11,4.5,4.61,5.17,4.53,5.33,5.14,4.81,4.17,4.41,3.59,5.87,3.83,6.03,4.89,4.32,4.69 );
    IntPtr rank = new IntPtr(0);
    DoublePtr qraux = new DoublePtr(0,0);
    IntPtr pivot = new IntPtr(1,2);
    DoublePtr work = new DoublePtr(0,0,0,0);
  //  rg.renjin.gcc.runtime.DoublePtr, org.renjin.gcc.runtime.IntPtr, org.renjin.gcc.runtime.IntPtr, org.renjin.gcc.runtime.DoublePtr, org.renjin.gcc.runtime.IntPtr, org.renjin.gcc.runtime.DoublePtr, org.renjin.gcc.runtime.DoublePtr, org.renjin.gcc.runtime.DoublePtr, org.renjin.gcc.runtime.DoublePtr, org.renjin.gcc.runtime.IntPtr, org.renjin.gcc.runtime.IntPtr, org.renjin.gcc.runtime.DoublePtr, org.renjin.gcc.runtime.DoublePtr
    call("dqrls_", x, n, p, y, ny, tol, coefficients, residuals, effects, rank, pivot, qraux, work);
//
//
//
//    Dqrls solution = new Dqrls(qr, n, p, y, ny, tol, coefficients, residuals, effects, pivot);
//
    System.out.println(x);

    System.out.println(residuals);

    assertThat(coefficients.array[0], closeTo(5.032, 0.0001));

  }
  
  private void call(String name, Object... args) throws Exception {
    for(Method method : Class.forName("org.renjin.appl.Appl").getMethods()) {
      if(method.getName().equals(name)) {
        method.invoke(null, args);
        return;
      }
    }
    throw new AssertionError("no method: " + name);
    
  }
  
}
