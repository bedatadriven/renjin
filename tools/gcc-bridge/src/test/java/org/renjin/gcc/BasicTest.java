package org.renjin.gcc;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static java.lang.Double.NaN;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BasicTest extends AbstractGccTest {

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

    Method method = clazz.getMethod("sum_array", DoublePtr.class, int.class);
    Double result = (Double) method.invoke(null, new DoublePtr(15, 20, 300), 3);

    assertThat(result, equalTo(335d));

    Method fillMethod = clazz.getMethod("fill_array", DoublePtr.class, int.class);
    DoublePtr ptr = new DoublePtr(new double[5]);
    fillMethod.invoke(null, ptr, ptr.array.length);

    System.out.println(Arrays.toString(ptr.array));
    
    
    Method mallocMethod = clazz.getMethod("malloc_test");
    result = (Double)mallocMethod.invoke(null);


    System.out.println("malloc result = " + result);

    assertThat(result, equalTo(7623d));
    

    // global_malloc_test()
    clazz.getMethod("malloc_global_test").invoke(null);


    result = (Double)clazz.getMethod("malloc_global_test2").invoke(null);
    assertThat(result, equalTo(7623d));
  }
  
  @Test
  public void pointersToPointers() throws Exception {
    Class clazz = compile("pointerpointer.c", "PointerPointers");
    Method method = clazz.getMethod("test");

    Double result = (Double) method.invoke(null);

    assertThat(result, equalTo(45.0));
  }

  @Test
  public void functionPointers() throws Exception {
    Class clazz = compile("funptr.c", "FunPtr");
    Method method = clazz.getMethod("sum_array", DoublePtr.class, int.class);
    Double result = (Double) method.invoke(null, new DoublePtr(1, 4, 16), 3);
    assertThat(result, equalTo(273d));
  }
  
  @Test
  public void structTest() throws Exception {
    Class clazz = compile("structs.c", "Structs");
    Method method = clazz.getMethod("test_account_value");
    Double result = (Double) method.invoke(null);
    assertThat(result, equalTo(5000d));
  }

  @Test
  public void arraysNonZeroLowerBound() throws Exception {
    Class clazz = compile("lbound.f", "LBound");

    Method test = clazz.getMethod("test_", DoublePtr.class, IntPtr.class);
    DoublePtr x = new DoublePtr( 0,0,0,0  );
    test.invoke(null, x, new IntPtr(4));

    assertThat(x.array[0], equalTo(1d*3d));
    assertThat(x.array[1], equalTo(2d*3d));
    assertThat(x.array[2], equalTo(3d*3d));
    assertThat(x.array[3], equalTo(4d*3d));


    System.out.println(x);
  }
  
  @Test
  public void calls() throws Exception {

    Class clazz = compile("calls.c", "Calls");
    Method sqrtMethod = clazz.getMethod("testsqrt", double.class);
    assertThat((Double) sqrtMethod.invoke(null, 4d), equalTo(3d));

  }

  @Test
  public void boolToInt() throws Exception {
    Class clazz = compile("bool2int.c", "BoolInt");

    Method method = clazz.getMethod("test", int.class);
    int result = (Integer) method.invoke(null, 0);

    assertThat(result, equalTo(5));
  }

  @Test
  public void logicalToInt() throws Exception {
    Class clazz = compile("bool2int.f", "LogicalInt");
    Method method = clazz.getMethod("test_", IntPtr.class, IntPtr.class);

    IntPtr x = new IntPtr(43);
    IntPtr y = new IntPtr(0);

    method.invoke(null, x, y);

  }

  @Test  
  public void switchStatement() throws Exception {
    Class clazz = compile("switch.c", "SwitchTest");

    Method distance = clazz.getMethod("R_distance", IntPtr.class, int.class, int.class);

    assertThat((Integer)distance.invoke(null, new IntPtr(1), 13, 14), equalTo(1));
    assertThat((Integer) distance.invoke(null, new IntPtr(2), 3, 4), equalTo(-1));

  }

  @Test
  public void logicalMod() throws  Exception {
    Class clazz = compile("logical.f", "Logical");

    clazz.getMethod("runtest_").invoke(null);

    Method iftest = clazz.getMethod("iftest_", IntPtr.class, IntPtr.class);
    IntPtr x = new IntPtr(0);

    iftest.invoke(null, new IntPtr(12), x);

    assertThat(x.unwrap(), equalTo(1));

  }

  @Test
  public void chars() throws Exception {
    Class clazz = compile("chars.c", "Chars");

    Method method = clazz.getMethod("circle_name");
    BytePtr ptr = (BytePtr) method.invoke(null);

    assertThat(ptr.nullTerminatedString(), equalTo("Hello world"));

    method = clazz.getMethod("test_first_char");
    Integer result = (Integer) method.invoke(null);

    assertThat(result, equalTo((int) 'h'));

    method = clazz.getMethod("unmarshall");
    result = (Integer) method.invoke(null);

    assertThat(result, equalTo((int) 'e'));
  }
  
  @Test
  @Ignore("not clear what correct behavior is for NaN values")
  public void fortranDoubleMax() throws Exception {
    Class clazz = compile("max.f", "MaxTest");
    Method method = clazz.getMethod("testmax", DoublePtr.class);
    
    DoublePtr x = new DoublePtr(-1);
    method.invoke(null, x);

    assertThat(x.unwrap(), equalTo(0d));
    
    x = new DoublePtr(Double.NaN);
    method.invoke(null, x);
    
    System.out.println(x.unwrap());
    
    assertTrue(Double.isNaN(x.unwrap()));
  }

  @Test
  public void fortran2darrays() throws Exception {
    Class clazz = compile("2darray.f", "ArrayTest");

    Method method = clazz.getMethod("test_", DoublePtr.class, IntPtr.class);
    
    double[] x = new double[9];

    method.invoke(null, new DoublePtr(x, 0), new IntPtr(3));
    
    System.out.println(Arrays.toString(x));
    
    assertThat(x[0], equalTo(1d));
    assertThat(x[4], equalTo(4d));
    assertThat(x[8], equalTo(9d));

    DoublePtr y = new DoublePtr(0);
    method = clazz.getMethod("localarray_", DoublePtr.class);
    method.invoke(null, y);

    assertThat(y.unwrap(), equalTo(110d));
  }

  @Test
  public void arrayC() throws Exception {
    Class clazz = compile("array.c", "ArrayCTest");

    Method method = clazz.getMethod("test");

    int result = (Integer) method.invoke(null);

    assertThat(result, equalTo(342));

  }

  @Test
  public void negate() throws Exception {
    Class clazz = compile("negate.c", "Negate");

    Method method = clazz.getMethod("negate", double.class);
    assertThat((Double) method.invoke(null, 1.5), equalTo(-1.5));
    assertThat((Double) method.invoke(null, -1.5), equalTo(1.5));
  }

  @Test
  public void globals() throws Exception {
    Class clazz = compile("globals.c", "Globals");

    Method magic_number = clazz.getMethod("magic_number");
    System.out.println(magic_number.invoke(null));
  }

  @Test
  public void enums() throws Exception {
    Class clazz = compile("enum.c", "EnumTest");

    Method method = clazz.getMethod("test", int.class);

    assertThat((Integer)method.invoke(null, 3), equalTo(1));
    assertThat((Integer) method.invoke(null, -1), equalTo(0));


  }

  @Test
  public void approx() throws Exception {
    Class clazz = compile("approx.c", "Approx");

  }

  @Test
  public void kmeans() throws Exception {
    Class clazz = compile("kmns.f", "KmeansFortran");

  }

  @Test
  public void fpComparison() throws Exception {
    Class clazz = compile("fpcmp.c", "FpCmp");

    assertThat(call(clazz, "lessThan", -2.4, -2.3), equalTo(1));
    assertThat(call(clazz, "lessThan", -2.4, -2.4), equalTo(0));
    assertThat(call(clazz, "lessThan", 1.1, 1.2), equalTo(1));
    assertThat(call(clazz, "lessThan", 1.5, 1.2), equalTo(0));
    assertThat(call(clazz, "lessThan", NaN, NaN), equalTo(0));
    assertThat(call(clazz, "lessThan", NaN, 42), equalTo(0));

    assertThat(call(clazz, "flessThan", -2.4f, -2.3f), equalTo(1));
    assertThat(call(clazz, "flessThan", -2.4f, -2.4f), equalTo(0));

    assertThat(call(clazz, "lessThanEqual", -2.4, -2.3), equalTo(1));
    assertThat(call(clazz, "lessThanEqual", -2.4, -2.4), equalTo(1));
    assertThat(call(clazz, "lessThanEqual", 1.1, 1.2), equalTo(1));
    assertThat(call(clazz, "lessThanEqual", 1.5, 1.2), equalTo(0));
    assertThat(call(clazz, "lessThanEqual", NaN, NaN), equalTo(0));
    assertThat(call(clazz, "lessThanEqual", NaN, 42), equalTo(0));

    assertThat(call(clazz, "greaterThan", -2.4, -2.3), equalTo(0));
    assertThat(call(clazz, "greaterThan", -2.4, -2.4), equalTo(0));
    assertThat(call(clazz, "greaterThan", 1.1, 1.2), equalTo(0));
    assertThat(call(clazz, "greaterThan", 1.5, 1.2), equalTo(1));
    assertThat(call(clazz, "greaterThan", NaN, NaN), equalTo(0));
    assertThat(call(clazz, "greaterThan", NaN, 42), equalTo(0));

    assertThat(call(clazz, "greaterThanEqual", -2.4, -2.3), equalTo(0));
    assertThat(call(clazz, "greaterThanEqual", -2.4, -2.4), equalTo(1));
    assertThat(call(clazz, "greaterThanEqual", 1.1, 1.2), equalTo(0));
    assertThat(call(clazz, "greaterThanEqual", 1.5, 1.2), equalTo(1));
    assertThat(call(clazz, "greaterThanEqual", NaN, NaN), equalTo(0));
    assertThat(call(clazz, "greaterThanEqual", NaN, 42), equalTo(0));

    assertThat(call(clazz, "truncate", 1.1), equalTo(1));
    assertThat(call(clazz, "truncate", 1.99), equalTo(1));

  }

  @Test
  public void bitwiseOperators() throws Exception {
    Class clazz = compile("bitwiseops.c", "Bitwise");
    
    assertThat(call(clazz, "bitwise_lshift", 16, 2), equalTo(16 << 2));
    assertThat(call(clazz, "bitwise_rshift", 16, 2), equalTo(16 >> 2));
    assertThat(call(clazz, "bitwise_xor", 16, 1024), equalTo(16 ^ 1024));
    assertThat(call(clazz, "bitwise_not", 4096), equalTo(~4096));
  }
  
  @Test
  public void hclust() throws Exception {
    Class clazz = compile("hclust.f", "HClust");

//       SUBROUTINE HCLUST(R, DMIN)

    Method hclust = clazz.getMethod("hclust_", DoublePtr.class, DoublePtr.class);
    
    DoublePtr r = new DoublePtr(43.4);
    DoublePtr dmin = new DoublePtr(0);
    
    hclust.invoke(null, r, dmin);
    
    assertThat(dmin.unwrap(), equalTo(r.unwrap()));

  }
  
  @Test
  public void cpp() throws Exception {
    Class clazz = compile("rect.cpp", "RectTest");

    Method calc_area = clazz.getMethod("calc_area");

    Integer result = (Integer) calc_area.invoke(null);

    
    assertThat(result, equalTo(12));
  }

  @Test
  @Ignore
  public void virtualCpp() throws Exception {

    Class clazz = compile("shape.cpp", "RectTest");

    Method calc_area = clazz.getMethod("calc_areas");

    Integer result = (Integer) calc_area.invoke(null);


    assertThat(result, equalTo(12));
  }
  
  @Test
  public void voidInference() throws Exception {
      compile("lamix.f", "Lamix");
  }
  
  @Test
  public void doubleComplex() throws Exception {
    Class clazz = compile("double_complex.f", "DoubleComplex");

    Method dcabs = clazz.getMethod("dcabs1_", double[].class, int.class);
    assertThat((Double) dcabs.invoke(null, new double[]{-1, 1}, 0), equalTo(2.0));
    assertThat((Double) dcabs.invoke(null, new double[]{1, 0}, 0), equalTo(1.0));
    assertThat((Double) dcabs.invoke(null, new double[]{0, 3}, 0), equalTo(3.0));
    
    Method clast = clazz.getMethod("clast_", double[].class, int.class, IntPtr.class);
    double[] x = {1, 2, 3, 4, 5, 6};
    IntPtr n = new IntPtr(3);
    double[] last = (double[]) clast.invoke(null, x, 0, n);
    
    assertThat(last[0], equalTo(5.0));
    assertThat(last[1], equalTo(6.0));
    
    
    // Check comparisons
    double[] a = new double[] { 1, 4};
    double[] b = new double[] { 5, 6};
    
    Method ceq = clazz.getMethod("ceq_", double[].class, int.class, double[].class, int.class);
    Method cne = clazz.getMethod("cne_", double[].class, int.class, double[].class, int.class);

    assertThat((Integer)ceq.invoke(null, a, 0, b, 0), equalTo(0));
    assertThat((Integer)ceq.invoke(null, a, 0, a, 0), equalTo(1));
    assertThat((Integer)ceq.invoke(null, b, 0, x, 4), equalTo(1));

    assertThat((Integer)cne.invoke(null, a, 0, b, 0), equalTo(1));
    assertThat((Integer)cne.invoke(null, a, 0, a, 0), equalTo(0));
    assertThat((Integer)cne.invoke(null, b, 0, x, 4), equalTo(0));

    // Check binary operations
    Method cmult = clazz.getMethod("cmul_", double[].class, int.class, double[].class, int.class);

    double product[] = (double[]) cmult.invoke(null, a, 0, b, 0);
    assertThat(product[0], equalTo(-19d));
    assertThat(product[1], equalTo(+26d));

    Method cadd = clazz.getMethod("cadd_", double[].class, int.class, double[].class, int.class);

    double sum[] = (double[]) cadd.invoke(null, a, 0, b, 0);
    assertThat(sum[0], equalTo(6d));
    assertThat(sum[1], equalTo(10d));
  }
  
  @Test
  public void updateComplexArrayPointer() throws Exception {
    Class clazz = compile("complex_update.f", "ComplexUpdate");

    double[] a = new double[]{ 1, 4, 3, -4, 5, -9};
    double[] b = new double[]{-14, -13};

    Method update = clazz.getMethod("update2_", double[].class, int.class, double[].class, int.class);
    
    update.invoke(null, a, 0, b, 0);
    
    assertThat(a[2], equalTo(b[0]));
    assertThat(a[3], equalTo(b[1]));


  }
  
  @Test
  public void singleComplex() throws Exception {
    Class clazz = compile("complex.f", "Complex");

    Method dcabs = clazz.getMethod("dcabs1_", float[].class, int.class);
    assertThat((Float) dcabs.invoke(null, new float[]{-1, 1}, 0), equalTo(2f));
    assertThat((Float) dcabs.invoke(null, new float[]{1, 0}, 0), equalTo(1f));
    assertThat((Float) dcabs.invoke(null, new float[]{0, 3}, 0), equalTo(3f));

    Method clast = clazz.getMethod("clast_", float[].class, int.class, IntPtr.class);
    float[] x = {1, 2, 3, 4, 5, 6};
    IntPtr n = new IntPtr(3);
    float[] last = (float[]) clast.invoke(null, x, 0, n);

    assertThat(last[0], equalTo(5f));
    assertThat(last[1], equalTo(6f));
  }
 
  @Test
  public void fortranStrings() throws Exception {
    Class clazz = compile("strings.f", "FortranStrings");
    
    try {
      Method method = clazz.getMethod("call_xerbla__");
      method.invoke(null);
    } catch (InvocationTargetException wrapper) {
      RuntimeException e = (RuntimeException) wrapper.getCause();
      assertThat(e.getMessage(), equalTo("** On entry to ZGERC parameter number 1 had an illegal value"));
    }
  }
  
  @Test
  public void lsame() throws Exception {
    Class clazz = compile("lsame.f", "Lsame");
    Method lsame = clazz.getMethod("lsame_", BytePtr.class, BytePtr.class, int.class, int.class);
    
    lsame.invoke(null, BytePtr.asciiString("A"), BytePtr.asciiString("a"), 1, 1);
    
  }
  
  @Test
  public void cher() throws Exception {
    Class clazz = compile("cher.f", "Cher");

  }

  @Test
  public void scnrm2() throws Exception {
    Class clazz = compile("scnrm2.f", "Scnrm2");

  }
  
  @Test
  public void fortranEquivalence() throws Exception {
    Class clazz = compile("equivalence.f", "Equivalence");
    Method testMethod = clazz.getMethod("test_");
    testMethod.invoke(null);
  }
}
