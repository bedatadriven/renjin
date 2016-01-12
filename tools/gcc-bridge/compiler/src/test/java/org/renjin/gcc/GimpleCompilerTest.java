package org.renjin.gcc;

import com.google.common.base.Charsets;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.ObjectPtr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static java.lang.Double.NaN;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GimpleCompilerTest extends AbstractGccTest {

  @Test
  public void sanitizeClassName() {
    assertThat(GimpleCompiler.sanitize("bit-ops"), equalTo("bit_ops"));
    assertThat(GimpleCompiler.sanitize("survey"), equalTo("survey"));
    assertThat(GimpleCompiler.sanitize("12345"), equalTo("_12345"));
  }
  
  @Test
  public void simpleTest() throws Exception {

    Class clazz = compile("area.c");

    // try to load class
    Method method = clazz.getMethod("circle_area", double.class);

    Double value = (Double) method.invoke(null, 2d);

    assertThat(value, closeTo(12.56, 0.01));
  }

  @Test
  public void pointers() throws Exception {
    Class clazz = compile("pointers.c");
 
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
    
    
    Method reallocTest = clazz.getMethod("realloc_test");
    Double reallocResult = (Double)reallocTest.invoke(null);
    
    assertThat(reallocResult, equalTo(41d+42d+43d+44d));
    
    // pointer comparison
    Method testCmp = clazz.getMethod("test_cmp");
    Integer cmpResult = (Integer) testCmp.invoke(null);

    assertThat(cmpResult, equalTo(1));
  }
  
  @Test
  public void loglik() throws Exception {
    Class clazz = compile("loglik.c");

    DoublePtr loglik = (DoublePtr) clazz.getMethod("loglik_test").invoke(null);
    assertThat(loglik.array.length, equalTo(2));
    assertThat(loglik.get(1), equalTo(34d));
    assertThat(loglik.get(0), equalTo(34d));
  }
  
  @Test
  public void pointersToPointers() throws Exception {
    Class clazz = compile("pointerpointer.c");
    Method method = clazz.getMethod("test");

    Double result = (Double) method.invoke(null);

    assertThat(result, equalTo(45.0));
  }
    
  @Test
  public void returningPointersToPointers() throws Exception {
    Class clazz = compile("cmatrix.c");
    Method cmatrix = clazz.getMethod("cmatrix", DoublePtr.class, int.class, int.class);
    DoublePtr array = new DoublePtr(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    ObjectPtr matrix = (ObjectPtr) cmatrix.invoke(null, array, 2, 5);
    DoublePtr row0 = (DoublePtr) matrix.array[matrix.offset];
    DoublePtr row1 = (DoublePtr) matrix.array[matrix.offset+1];

    assertThat(row0.get(0), equalTo(1d));
    assertThat(row0.get(1), equalTo(2d));
    
    assertThat(row1.get(0), equalTo(6d));
    assertThat(row1.get(1), equalTo(7d));

    Method get_at = clazz.getMethod("get_at", ObjectPtr.class, int.class, int.class);

    DoublePtr prow0 = (DoublePtr) get_at.invoke(null, matrix, 0, 0);
    assertThat(prow0.array, is(prow0.array));
    assertThat(prow0.offset, equalTo(0));

    Method sum_second_col = clazz.getMethod("sum_second_col", DoublePtr.class, int.class, int.class);
    double sum = (Double)sum_second_col.invoke(null, array, 2, 5);
    
    assertThat(sum, equalTo(9d));
    
  }

  @Test
  public void functionPointers() throws Exception {
    Class clazz = compile("funptr.c");
    Method method = clazz.getMethod("sum_array", DoublePtr.class, int.class);
    Double result = (Double) method.invoke(null, new DoublePtr(1, 4, 16), 3);
    assertThat(result, equalTo(273d));
  }
  
  @Test
  public void structTest() throws Exception {
    Class clazz = compile("structs.c");
    Method method = clazz.getMethod("test_account_value");
    Double result = (Double) method.invoke(null);
    assertThat(result, equalTo(5000d));
    
    Method testMalloc = clazz.getMethod("test_malloc");
    result = (Double)testMalloc.invoke(null);
    assertThat(result, equalTo(15000d));
  }
  
  @Test
  public void static_init() throws Exception {
    Class clazz = compile("static_init.c");
    
    Method testName = clazz.getMethod("test_name");
    BytePtr name = (BytePtr) testName.invoke(null);
    assertThat(name.nullTerminatedString(), equalTo("square"));

    Method testArray = clazz.getMethod("test_array");
    BytePtr element = (BytePtr) testArray.invoke(null);
    assertThat(element.nullTerminatedString(), equalTo("loglik"));
  }

  @Test
  public void arraysNonZeroLowerBound() throws Exception {
    Class clazz = compile("lbound.f");

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
  public void discardReturnValue() throws Exception {
    Class clazz = compile("discardReturn.c");
    Method run = clazz.getMethod("run");

    Integer returnValue = (Integer)run.invoke(null);
  
    assertThat(returnValue, equalTo(0));
  }
  
  @Test
  public void calls() throws Exception {

    Class clazz = compile("calls.c");
    Method sqrtMethod = clazz.getMethod("testsqrt", double.class);
    assertThat((Double) sqrtMethod.invoke(null, 4d), equalTo(3d));

  }

  @Test
  public void boolToInt() throws Exception {
    Class clazz = compile("cbool2int.c");

    Method method = clazz.getMethod("test", int.class);
    int result = (Integer) method.invoke(null, 0);

    assertThat(result, equalTo(5));
  }

  @Test
  public void logicalToInt() throws Exception {
    Class clazz = compile("bool2int.f");
    Method method = clazz.getMethod("test_", IntPtr.class, IntPtr.class);

    IntPtr x = new IntPtr(43);
    IntPtr y = new IntPtr(0);

    method.invoke(null, x, y);

  }

  @Test  
  public void switchStatement() throws Exception {
    Class clazz = compile("switch.c");

    Method distance = clazz.getMethod("R_distance", IntPtr.class, int.class, int.class);

    assertThat((Integer) distance.invoke(null, new IntPtr(1), 13, 14), equalTo(1));
    assertThat((Integer) distance.invoke(null, new IntPtr(2), 3, 4), equalTo(-1));
  }

  @Test
  public void logicalMod() throws  Exception {
    Class clazz = compile("logical.f");

    clazz.getMethod("runtest_").invoke(null);

    Method iftest = clazz.getMethod("iftest_", IntPtr.class, IntPtr.class);
    IntPtr x = new IntPtr(0);

    iftest.invoke(null, new IntPtr(12), x);

    assertThat(x.unwrap(), equalTo(1));

  }
  
  @Test
  public void logicalAnd() throws Exception {
    Class clazz = compile("and.c");
    Method testMethod = clazz.getMethod("test", int.class, double.class);

    assertThat((Integer)testMethod.invoke(null, 1, 0d), equalTo(0));
    assertThat((Integer) testMethod.invoke(null, 1, 3d), equalTo(41));
    assertThat((Integer)testMethod.invoke(null, -1, 3d), equalTo(0));
  }

  @Test
  public void logicalOr() throws Exception {
    Class clazz = compile("or.f");
    Method testMethod = clazz.getMethod("stlest_", IntPtr.class, IntPtr.class, DoublePtr.class);

    DoublePtr result = new DoublePtr(0);
    
    testMethod.invoke(null, new IntPtr(41), new IntPtr(42), result);
    assertThat(result.unwrap(), equalTo(42.0));

    testMethod.invoke(null, new IntPtr(49), new IntPtr(42), result);
    assertThat(result.unwrap(), equalTo(49.0));
  }


  @Test
  public void chars() throws Exception {
    Class clazz = compile("chars.c");

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
    Class clazz = compile("max.f");
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
    Class clazz = compile("two_d_array.f");

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
    Class clazz = compile("array.c");

    Method method = clazz.getMethod("test");

    int result = (Integer) method.invoke(null);

    assertThat(result, equalTo(342));
    
    Method testPointer = clazz.getMethod("test_pointer");
    result = (Integer) testPointer.invoke(null);
    
    assertThat(result, equalTo(42));

  }

  @Test
  public void negate() throws Exception {
    Class clazz = compile("negate.c");

    Method method = clazz.getMethod("negate", double.class);
    assertThat((Double) method.invoke(null, 1.5), equalTo(-1.5));
    assertThat((Double) method.invoke(null, -1.5), equalTo(1.5));
  }

  @Test
  public void globals() throws Exception {
    Class clazz = compile("globals.c");

    Method magic_number = clazz.getMethod("magic_number");
    Integer result = (Integer) magic_number.invoke(null);
    
    assertThat(result, equalTo(42));
    
  }

  @Test
  public void enums() throws Exception {
    Class clazz = compile("enum.c");

    Method method = clazz.getMethod("test", int.class);

    assertThat((Integer)method.invoke(null, 3), equalTo(1));
    assertThat((Integer) method.invoke(null, -1), equalTo(0));


  }

  @Test
  public void approx() throws Exception {
    Class clazz = compile("approx.c");

  }

  @Test
  public void kmeans() throws Exception {
    Class clazz = compile("kmns.f");

  }

  @Test
  public void fpComparison() throws Exception {
    Class clazz = compile("fpcmp.c");

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
    Class clazz = compile("bitwiseops.c");
    
    assertThat(call(clazz, "bitwise_lshift", 16, 2), equalTo(16 << 2));
    assertThat(call(clazz, "bitwise_rshift", 16, 2), equalTo(16 >> 2));
    assertThat(call(clazz, "bitwise_xor", 16, 1024), equalTo(16 ^ 1024));
    assertThat(call(clazz, "bitwise_not", 4096), equalTo(~4096));
  }
  
  @Test
  public void hclust() throws Exception {
    Class clazz = compile("hclust.f");

//       SUBROUTINE HCLUST(R, DMIN)

    Method hclust = clazz.getMethod("hclust_", DoublePtr.class, DoublePtr.class);
    
    DoublePtr r = new DoublePtr(43.4);
    DoublePtr dmin = new DoublePtr(0);
    
    hclust.invoke(null, r, dmin);
    
    assertThat(dmin.unwrap(), equalTo(r.unwrap()));

  }
  
  @Test
  public void cpp() throws Exception {
    Class clazz = compile("rect.cpp");

    Method calc_area = clazz.getMethod("calc_area");

    Integer result = (Integer) calc_area.invoke(null);

    
    assertThat(result, equalTo(12));
  }

  @Test
  @Ignore("wip")
  public void virtualCpp() throws Exception {

    Class clazz = compile("shape.cpp");

    Method calc_area = clazz.getMethod("calc_areas");

    Integer result = (Integer) calc_area.invoke(null);


    assertThat(result, equalTo(12));
  }
  
  @Test
  public void voidInference() throws Exception {
      compile("lamix.f");
  }
  
  @Test
  public void doubleComplex() throws Exception {
    Class clazz = compile("double_complex.f");

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
    Class clazz = compile("complex_update.f");

    double[] a = new double[]{ 1, 4, 3, -4, 5, -9};
    double[] b = new double[]{-14, -13};

    Method update = clazz.getMethod("update2_", double[].class, int.class, double[].class, int.class);
    
    update.invoke(null, a, 0, b, 0);
    
    assertThat(a[2], equalTo(b[0]));
    assertThat(a[3], equalTo(b[1]));


  }
  
  @Test
  public void singleComplex() throws Exception {
    Class clazz = compile("complex.f");

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
    Class clazz = compile("strings.f");
    
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
    Class clazz = compile("lsame.f");
    Method lsame = clazz.getMethod("lsame_", BytePtr.class, BytePtr.class, int.class, int.class);
    
    lsame.invoke(null, BytePtr.asciiString("A"), BytePtr.asciiString("a"), 1, 1);
    
  }
  
  @Test
  public void cher() throws Exception {
    Class clazz = compile("cher.f");

  }

  @Test
  public void scnrm2() throws Exception {
    Class clazz = compile("scnrm2.f");

  }
  
  @Test
  @Ignore("todo: unions")
  public void fortranEquivalence() throws Exception {
    Class clazz = compile("equivalence.f");
    Method testMethod = clazz.getMethod("test_");
    testMethod.invoke(null);
  }
  
  @Test
  public void linking() throws Exception {
    compile(Arrays.asList("link1.c", "link2.c"));

    Class<?> link1 = Class.forName("org.renjin.gcc.link1");
    Class<?> link2 = Class.forName("org.renjin.gcc.link2");
    
    Integer test1 = (Integer) link1.getMethod("test").invoke(null);
    assertThat(test1, equalTo(3));

    Integer test2 = (Integer) link2.getMethod("test").invoke(null);
    assertThat(test2, equalTo(2));

    Double result = (Double) link2.getMethod("test_points").invoke(null);
    assertThat(result, equalTo(41d));
  }
  
  @Test
  public void addressableFields() throws Exception {
    Class clazz = compile("field_address.c");

    Method testMethod = clazz.getMethod("test");
    Object result = testMethod.invoke(null);
    
  }
  
  @Test
  public void uninitializedVariables() throws Exception {
//
//    GimpleCompilationUnit unit = Iterables.getOnlyElement(compileToGimple(Lists.newArrayList("uninit.c")));
//    GimpleFunction function = Iterables.getOnlyElement(unit.getFunctions());
//
//    ControlFlowGraph cfg = new ControlFlowGraph(function);
//    cfg.dumpGraph(new File("/tmp/uninit.dot"));
//
//    InitDataFlowAnalysis analysis = new InitDataFlowAnalysis(function, cfg);
//    analysis.solve();
//    analysis.dump();
//    
//    System.out.println("Variables requiring initialization: " + analysis.getVariablesUsedWithoutInitialization());

    Class<?> clazz = compile("uninit.c");
    Method testMethod = clazz.getMethod("test_uninitialized");
    testMethod.invoke(null);
  }
  
  @Test
  public void memcpy() throws Exception {
    Class clazz = compile("memcpy.c");
    
    Method test = clazz.getMethod("test_memcpy", null);
    Integer result = (Integer) test.invoke(null);
    
    assertThat(result, equalTo(1));
  }
  
  @Test
  public void varArgsCalls() throws Exception {
    Class clazz = compile("varargs.c");
    
    Method test = clazz.getMethod("test_sprintf", BytePtr.class, int.class);

    BytePtr message = (BytePtr) test.invoke(null, BytePtr.nullTerminatedString("Bob", Charsets.US_ASCII), 99);

    assertThat(message.nullTerminatedString(), equalTo("Hello Bob, you have 99 messages"));
  }

  @Test
  public void ctypes() throws Exception {
    Class clazz = compile("ctype.c");
    Method countWhitespace = clazz.getMethod("count_whitespace", BytePtr.class);
    assertThat((Integer)countWhitespace.invoke(null, 
        BytePtr.nullTerminatedString("Hello World!", Charsets.US_ASCII)), equalTo(1));
  }
  
  @Test
  public void unsigned() throws Exception {
    Class clazz = compile("unsigned.c");
    
    // check conversions
    Method bitflip = clazz.getMethod("bitflip", double.class);
    assertThat((Double) bitflip.invoke(null, 0d), equalTo(4294967295d));
    
    Method bitand = clazz.getMethod("bitand", double.class, double.class);
    assertThat((Double) bitand.invoke(null, 0d, 4294967295d), equalTo(0d));
    
    // check double to unsigned int and back
    Method unsignedIntRoundTrip = clazz.getMethod("unsignedIntRoundTrip", double.class);
    assertThat((Double)unsignedIntRoundTrip.invoke(null, 0d), equalTo(0d));
    assertThat((Double) unsignedIntRoundTrip.invoke(null, -1), equalTo(4294967295d));
    assertThat((Double) unsignedIntRoundTrip.invoke(null, 2147483653d), equalTo(2147483653d));
  }
  

  @Test
  public void unsignedToSigned() throws Exception {
    Class clazz = compile("to_signed.c");
    Method charToUnsigned16 = clazz.getMethod("charToUnsigned16", byte.class);
    
    assertThat((Integer)charToUnsigned16.invoke(null, (byte) 0), equalTo(0));
    assertThat((Integer) charToUnsigned16.invoke(null, (byte) 120), equalTo(120));
    assertThat((Integer) charToUnsigned16.invoke(null, (byte) -62), equalTo(65474));


    Method int16ToUnsigned32 = clazz.getMethod("int16ToUnsigned32", short.class);
    assertThat((Integer)int16ToUnsigned32.invoke(null, (short)0), equalTo(0));
    assertThat((Integer)int16ToUnsigned32.invoke(null, (short) 4096), equalTo(0x1000));
    assertThat((Integer)int16ToUnsigned32.invoke(null, (short) -34), equalTo(0xffffffde));
    assertThat((Integer) int16ToUnsigned32.invoke(null, (short) -4142), equalTo(0xffffefd2));
    
    Method uint32ToInt8 = clazz.getMethod("uint32ToInt8", int.class);
    assertThat((Byte)uint32ToInt8.invoke(null, 0), equalTo((byte)0));
    assertThat((Byte)uint32ToInt8.invoke(null, 3000), equalTo((byte)-72));
    assertThat((Byte)uint32ToInt8.invoke(null, 4096), equalTo((byte)0));
    assertThat((Byte) uint32ToInt8.invoke(null, 0xffffffff), equalTo((byte)-1));
    assertThat((Byte) uint32ToInt8.invoke(null, 0xfffffffe), equalTo((byte)-2));

    Method uint32ToInt16 = clazz.getMethod("uint32ToInt16", int.class);
    assertThat((Short)uint32ToInt16.invoke(null, 0), equalTo((short)0));
    assertThat((Short)uint32ToInt16.invoke(null, 0xbb8), equalTo((short)3000));
    assertThat((Short)uint32ToInt16.invoke(null, 0x40000), equalTo((short)0));

    Method uint32ToUint16 = clazz.getMethod("uint32ToUint16", int.class);
    assertThat((Character)uint32ToUint16.invoke(null, 0), equalTo((char)0));
    assertThat((Character)uint32ToUint16.invoke(null, 0x402), equalTo((char)1026));
    assertThat((Character)uint32ToUint16.invoke(null, 0x1000), equalTo((char)4096));
    assertThat((Character)uint32ToUint16.invoke(null, 0xFFFF), equalTo((char)65535));
    assertThat((Character)uint32ToUint16.invoke(null, 0x10003), equalTo((char)3));
    
    Method uint32ToUint64 = clazz.getMethod("uint32ToUint64", int.class);
    assertThat((Long)uint32ToUint64.invoke(null, 0), equalTo((long)0));
    assertThat((Long)uint32ToUint64.invoke(null, 0xFF), equalTo(0xFFL));
    assertThat((Long)uint32ToUint64.invoke(null, 0xFFFFFFFF), equalTo(0xFFFFFFFFL));

    Method uint16ToUint64 = clazz.getMethod("uint32ToUint64", int.class);
    assertThat((Long)uint32ToUint64.invoke(null, 0), equalTo((long)0));
    assertThat((Long)uint32ToUint64.invoke(null, 0xFF), equalTo(0xFFL));
    assertThat((Long)uint32ToUint64.invoke(null, 0xFFFFFFFF), equalTo(0xFFFFFFFFL));


  }
}
