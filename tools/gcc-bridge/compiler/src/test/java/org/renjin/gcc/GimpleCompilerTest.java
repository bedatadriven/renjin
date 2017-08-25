/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.gcc.runtime.*;
import org.renjin.repackaged.guava.base.Charsets;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static java.lang.Double.NaN;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.renjin.repackaged.guava.primitives.UnsignedBytes.checkedCast;

@SuppressWarnings("unchecked")
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

    Method method = clazz.getMethod("sum_array", Ptr.class, int.class);
    Double result = (Double) method.invoke(null, new DoublePtr(15, 20, 300), 3);

    assertThat(result, equalTo(335d));

    Method fillMethod = clazz.getMethod("fill_array", Ptr.class, int.class);
    DoublePtr ptr = new DoublePtr(new double[5]);
    fillMethod.invoke(null, ptr, ptr.array.length);

    System.out.println(Arrays.toString(ptr.array));


    Method mallocMethod = clazz.getMethod("malloc_test");
    result = (Double) mallocMethod.invoke(null);


    System.out.println("malloc result = " + result);

    assertThat(result, equalTo(7623d));


    // global_malloc_test()
    clazz.getMethod("malloc_global_test").invoke(null);


    result = (Double) clazz.getMethod("malloc_global_test2").invoke(null);
    assertThat(result, equalTo(7623d));


    Method reallocTest = clazz.getMethod("realloc_test");
    Double reallocResult = (Double) reallocTest.invoke(null);

    assertThat(reallocResult, equalTo(41d + 42d + 43d + 44d));

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
    Method cmatrix = clazz.getMethod("cmatrix", Ptr.class, int.class, int.class);
    DoublePtr array = new DoublePtr(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    Ptr matrix = (Ptr) cmatrix.invoke(null, array, 2, 5);
    Ptr row0 = matrix.getPointer(0);
    Ptr row1 = matrix.getPointer(4);

    assertThat(row0.getAlignedDouble(0), equalTo(1d));
    assertThat(row0.getAlignedDouble(1), equalTo(2d));

    assertThat(row1.getAlignedDouble(0), equalTo(6d));
    assertThat(row1.getAlignedDouble(1), equalTo(7d));

    Method get_at = clazz.getMethod("get_at", Ptr.class, int.class, int.class);

    DoublePtr prow0 = (DoublePtr) get_at.invoke(null, matrix, 0, 0);
    assertThat(prow0.array, is(prow0.array));
    assertThat(prow0.offset, equalTo(0));

    Method sum_second_col = clazz.getMethod("sum_second_col", Ptr.class, int.class, int.class);
    double sum = (Double) sum_second_col.invoke(null, array, 2, 5);

    assertThat(sum, equalTo(9d));

  }

  @Test
  public void functionPointers() throws Exception {
    compileAndTest("funptr.c");
  }

  @Test
  public void structTest() throws Exception {
    compileAndTest("structs.c");
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

    Method test = clazz.getMethod("test_", Ptr.class, Ptr.class);
    DoublePtr x = new DoublePtr(0, 0, 0, 0);
    test.invoke(null, x, new IntPtr(4));

    assertThat(x.array[0], equalTo(1d * 3d));
    assertThat(x.array[1], equalTo(2d * 3d));
    assertThat(x.array[2], equalTo(3d * 3d));
    assertThat(x.array[3], equalTo(4d * 3d));


    System.out.println(x);
  }

  @Test
  public void fortranMultiDimArrays() throws Exception {
    Class clazz = compile("lbound_alloc.f");

    Method test = clazz.getMethod("testprogram_");
    test.invoke(null);

  }


  @Test(expected = InvocationTargetException.class)
  public void files() throws Exception {
    compileAndTest("files.c");
  }

  @Test
  public void discardReturnValue() throws Exception {
    Class clazz = compile("discardReturn.c");
    Method run = clazz.getMethod("run");

    Integer returnValue = (Integer) run.invoke(null);

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
    Method method = clazz.getMethod("test_", Ptr.class, Ptr.class);

    IntPtr x = new IntPtr(43);
    IntPtr y = new IntPtr(0);

    method.invoke(null, x, y);

  }

  @Test
  public void switchStatement() throws Exception {
    Class clazz = compile("switch.c");

    Method distance = clazz.getMethod("R_distance", Ptr.class, int.class, int.class);

    assertThat((Integer) distance.invoke(null, new IntPtr(1), 13, 14), equalTo(1));
    assertThat((Integer) distance.invoke(null, new IntPtr(2), 3, 4), equalTo(-1));
  }

  @Test
  public void logicalMod() throws Exception {
    Class clazz = compile("logical.f");

    clazz.getMethod("runtest_").invoke(null);

    Method iftest = clazz.getMethod("iftest_", Ptr.class, Ptr.class);
    IntPtr x = new IntPtr(0);

    iftest.invoke(null, new IntPtr(12), x);

    assertThat(x.getInt(), equalTo(1));

  }

  @Test
  public void logicalAnd() throws Exception {
    Class clazz = compile("and.c");
    Method testMethod = clazz.getMethod("test", int.class, double.class);

    assertThat((Integer) testMethod.invoke(null, 1, 0d), equalTo(0));
    assertThat((Integer) testMethod.invoke(null, 1, 3d), equalTo(41));
    assertThat((Integer) testMethod.invoke(null, -1, 3d), equalTo(0));
  }

  @Test
  public void logicalOr() throws Exception {
    Class clazz = compile("or.f");
    Method testMethod = clazz.getMethod("stlest_", Ptr.class, Ptr.class, Ptr.class);

    DoublePtr result = new DoublePtr(0);

    testMethod.invoke(null, new IntPtr(41), new IntPtr(42), result);
    assertThat(result.getDouble(), equalTo(42.0));

    testMethod.invoke(null, new IntPtr(49), new IntPtr(42), result);
    assertThat(result.getDouble(), equalTo(49.0));
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

    Method method = clazz.getMethod("test_", Ptr.class, Ptr.class);

    double[] x = new double[9];

    method.invoke(null, new DoublePtr(x, 0), new IntPtr(3));

    System.out.println(Arrays.toString(x));

    assertThat(x[0], equalTo(1d));
    assertThat(x[4], equalTo(4d));
    assertThat(x[8], equalTo(9d));

    DoublePtr y = new DoublePtr(0);
    method = clazz.getMethod("localarray_", Ptr.class);
    method.invoke(null, y);

    assertThat(y.unwrap(), equalTo(110d));
  }

  @Test
  public void pruneCpp() throws Exception {
    compileAndTest("prune.cpp");
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
  public void dynamicArrays() throws Exception {
    compileAndTest("dynamic_arrays.c");
  }

  @Test
  public void array2d() throws Exception {
    compileAndTest("array2d.c");
  }

  @Test
  public void covDna() throws Exception {
    compileAndTest("cov_dna.c");
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

    assertThat((Integer) method.invoke(null, 3), equalTo(1));
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
    Class clazz = compileAndTest("fpcmp.c");

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
    assertThat(call(clazz, "bitwise_not_long", 32342355L), equalTo(-32342356L));


    assertThat(call(clazz, "byte_lshift", (byte) 1, (byte) 1), equalTo(2));
    assertThat(call(clazz, "byte_lshift", (byte) 1, (byte) 7), equalTo(0x80));
    assertThat(call(clazz, "byte_lshift", (byte) 1, (byte) 8), equalTo(0));
    assertThat(call(clazz, "byte_lshift", (byte) 1, (byte) 10), equalTo(0));

    Method bitwiseNotUint8 = clazz.getMethod("bitwise_not_uint8", byte.class);
    assertThat((Integer) bitwiseNotUint8.invoke(null, checkedCast(0x00)), equalTo(0xFF));
    assertThat((Integer) bitwiseNotUint8.invoke(null, checkedCast(0x01)), equalTo(0xFE));
    assertThat((Integer) bitwiseNotUint8.invoke(null, checkedCast(0xF)), equalTo(0xF0));
    assertThat((Integer) bitwiseNotUint8.invoke(null, checkedCast(0xF1)), equalTo(0x0E));
    assertThat((Integer) bitwiseNotUint8.invoke(null, checkedCast(0xFF)), equalTo(0x00));
  }

  @Test
  public void pointerCasting() throws Exception {
    compileAndTest("ptr_cast.c");
  }

  @Test(expected = ClassCastException.class)
  public void illegalPointerCast() throws Throwable {
    Class clazz = compile("illegal_cast.c");

    Method method = clazz.getMethod("do_cast");
    try {
      method.invoke(null);

    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
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
  public void rectCpp() throws Exception {
    compileAndTest("rect.cpp");
  }


  @Test
  public void rectSort() throws Exception {
    compileAndTest("rect_sort.cpp");
  }

  @Test
  public void overloadedMethods() throws Exception {
    Class clazz = compile("methods.cpp");

    Method add = clazz.getMethod("_Z3addii", int.class, int.class);
    Integer intSum = (Integer) add.invoke(null, Integer.valueOf(3), Integer.valueOf(5));
    assertThat(intSum, is(Integer.valueOf(8)));

    add = clazz.getMethod("_Z3addff", float.class, float.class);
    Float floatSum = (Float) add.invoke(null, Float.valueOf(3.1f), Float.valueOf(5.1f));
    assertThat(floatSum, is(Float.valueOf(8.2f)));
  }

  @Test
  public void virtualCpp() throws Exception {

    Class clazz = compile("shape.cpp");

    Method calc_area = clazz.getMethod("calc_areas");

    Integer result = (Integer) calc_area.invoke(null);


    assertThat(result, equalTo(532));
  }

  @Test
  public void unions() throws Exception {
    compileAndTest("unions.c");
  }

  @Test
  public void unionClass() throws Exception {
    compileAndTest("class_unions.c");
  }


  @Test
  public void builtinExpect() throws Exception {
    compileAndTest("expect.c");
  }

  @Test
  public void voidInference() throws Exception {
    compile("lamix.f");
  }

  @Test
  public void doubleComplex() throws Exception {
    Class clazz = compile("double_complex.f");

    Method dcabs = clazz.getMethod("dcabs1_", Ptr.class);
    assertThat((Double) dcabs.invoke(null, new DoublePtr(-1, 1)), equalTo(2.0));
    assertThat((Double) dcabs.invoke(null, new DoublePtr(1, 0)), equalTo(1.0));
    assertThat((Double) dcabs.invoke(null, new DoublePtr(0, 3)), equalTo(3.0));

    Method clast = clazz.getMethod("clast_", Ptr.class, Ptr.class);
    DoublePtr x = new DoublePtr(1, 2, 3, 4, 5, 6);
    IntPtr n = new IntPtr(3);
    double[] last = (double[]) clast.invoke(null, x, n);

    assertThat(last[0], equalTo(5.0));
    assertThat(last[1], equalTo(6.0));


    // Check comparisons
    DoublePtr a = new DoublePtr(1, 4);
    DoublePtr b = new DoublePtr(5, 6);

    Method ceq = clazz.getMethod("ceq_", Ptr.class, Ptr.class);
    Method cne = clazz.getMethod("cne_", Ptr.class, Ptr.class);

    assertThat((Integer) ceq.invoke(null, a, b), equalTo(0));
    assertThat((Integer) ceq.invoke(null, a, a), equalTo(1));
    assertThat((Integer) ceq.invoke(null, b, x.pointerPlus(4 * DoublePtr.BYTES)), equalTo(1));

    assertThat((Integer) cne.invoke(null, a, b), equalTo(1));
    assertThat((Integer) cne.invoke(null, a, a), equalTo(0));
    assertThat((Integer) cne.invoke(null, b, x.pointerPlus(4 * DoublePtr.BYTES)), equalTo(0));

    // Check binary operations
    Method cmult = clazz.getMethod("cmul_", Ptr.class, Ptr.class);

    double product[] = (double[]) cmult.invoke(null, a, b);
    assertThat(product[0], equalTo(-19d));
    assertThat(product[1], equalTo(+26d));

    Method cadd = clazz.getMethod("cadd_", Ptr.class, Ptr.class);

    double sum[] = (double[]) cadd.invoke(null, a, b);
    assertThat(sum[0], equalTo(6d));
    assertThat(sum[1], equalTo(10d));
  }

  @Test
  public void updateComplexArrayPointer() throws Exception {
    Class clazz = compile("complex_update.f");

    double[] a = new double[]{1, 4, 3, -4, 5, -9};
    double[] b = new double[]{-14, -13};

    Method update = clazz.getMethod("update2_", double[].class, int.class, double[].class, int.class);

    update.invoke(null, a, 0, b, 0);

    assertThat(a[2], equalTo(b[0]));
    assertThat(a[3], equalTo(b[1]));


  }

  @Test
  public void singleComplex() throws Exception {
    Class clazz = compile("complex.f");

    Method dcabs = clazz.getMethod("dcabs1_", Ptr.class);
    assertThat((Float) dcabs.invoke(null, new FloatPtr(-1, 1)), equalTo(2f));
    assertThat((Float) dcabs.invoke(null, new FloatPtr(1, 0)), equalTo(1f));
    assertThat((Float) dcabs.invoke(null, new FloatPtr(0, 3)), equalTo(3f));

    Method clast = clazz.getMethod("clast_", Ptr.class, Ptr.class);
    FloatPtr x = new FloatPtr(1, 2, 3, 4, 5, 6);
    IntPtr n = new IntPtr(3);
    float[] last = (float[]) clast.invoke(null, x, n);

    assertThat(last[0], equalTo(5f));
    assertThat(last[1], equalTo(6f));
  }

  @Test
  public void fortranStrings() throws Exception {
    Class clazz = compile("strings.f");

    try {
      Method method = clazz.getMethod("call_xerbla_");
      method.invoke(null);
    } catch (InvocationTargetException wrapper) {
      wrapper.printStackTrace();
      RuntimeException e = (RuntimeException) wrapper.getCause();
      assertThat(e.getMessage(), equalTo("** On entry to ZGERC parameter number 1 had an illegal value"));
    }
  }

  @Test
  public void stringsCpp() throws Exception {
    compileAndTest("std_strings.cpp");
  }


  @Test
  public void lsame() throws Exception {
    Class clazz = compile("lsame.f");
    Method lsame = clazz.getMethod("lsame_", Ptr.class, Ptr.class, int.class, int.class);

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
  @Ignore
  public void cppExceptions() throws Exception {
    compileAndTest("exceptions.cpp");
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

    Integer magicNumber1 = (Integer) link2.getMethod("test_global_var").invoke(null);
    assertThat(magicNumber1, equalTo(420));

    Integer magicNumber2 = (Integer) link2.getMethod("test_addressable_global_var").invoke(null);
    assertThat(magicNumber2, equalTo(24));
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

    Method test = clazz.getMethod("test_memcpy");
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
    Method countWhitespace = clazz.getMethod("count_whitespace", Ptr.class);
    assertThat((Integer) countWhitespace.invoke(null,
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
    assertThat((Double) unsignedIntRoundTrip.invoke(null, 0d), equalTo(0d));
    assertThat((Double) unsignedIntRoundTrip.invoke(null, -1), equalTo(4294967295d));
    assertThat((Double) unsignedIntRoundTrip.invoke(null, 2147483653d), equalTo(2147483653d));

    // From signed to unsigned
    Method int32_to_uint8 = clazz.getMethod("int32_to_uint8", int.class);
    assertThat((Integer) int32_to_uint8.invoke(null, -20), equalTo(236));

    // From uint32 to uint8
    Method uint32_to_uint8 = clazz.getMethod("uint32_to_uint8", int.class);
    assertThat((Integer) uint32_to_uint8.invoke(null, 0), equalTo(0));
    assertThat((Integer) uint32_to_uint8.invoke(null, 0x7F), equalTo(127));
    assertThat((Integer) uint32_to_uint8.invoke(null, 0x80), equalTo(128));
    assertThat((Integer) uint32_to_uint8.invoke(null, 0xFF), equalTo(255));
    assertThat((Integer) uint32_to_uint8.invoke(null, 0x100), equalTo(0));
    assertThat((Integer) uint32_to_uint8.invoke(null, 0x400), equalTo(0));
    assertThat((Integer) uint32_to_uint8.invoke(null, 0xFFFFFFFF), equalTo(255));
  }

  @Test
  public void unsignedComparison() throws Exception {
    compileAndTest("unsigned_comparison.c");
  }


  @Test
  public void unsignedToSigned() throws Exception {
    Class clazz = compile("to_signed.c");
    Method charToUnsigned16 = clazz.getMethod("charToUnsigned16", byte.class);

    assertThat((Integer) charToUnsigned16.invoke(null, (byte) 0), equalTo(0));
    assertThat((Integer) charToUnsigned16.invoke(null, (byte) 120), equalTo(120));
    assertThat((Integer) charToUnsigned16.invoke(null, (byte) -62), equalTo(65474));


    Method int16ToUnsigned32 = clazz.getMethod("int16ToUnsigned32", short.class);
    assertThat((Integer) int16ToUnsigned32.invoke(null, (short) 0), equalTo(0));
    assertThat((Integer) int16ToUnsigned32.invoke(null, (short) 4096), equalTo(0x1000));
    assertThat((Integer) int16ToUnsigned32.invoke(null, (short) -34), equalTo(0xffffffde));
    assertThat((Integer) int16ToUnsigned32.invoke(null, (short) -4142), equalTo(0xffffefd2));

    Method uint32ToInt8 = clazz.getMethod("uint32ToInt8", int.class);
    assertThat((Byte) uint32ToInt8.invoke(null, 0), equalTo((byte) 0));
    assertThat((Byte) uint32ToInt8.invoke(null, 3000), equalTo((byte) -72));
    assertThat((Byte) uint32ToInt8.invoke(null, 4096), equalTo((byte) 0));
    assertThat((Byte) uint32ToInt8.invoke(null, 0xffffffff), equalTo((byte) -1));
    assertThat((Byte) uint32ToInt8.invoke(null, 0xfffffffe), equalTo((byte) -2));

    Method uint32ToInt16 = clazz.getMethod("uint32ToInt16", int.class);
    assertThat((Short) uint32ToInt16.invoke(null, 0), equalTo((short) 0));
    assertThat((Short) uint32ToInt16.invoke(null, 0xbb8), equalTo((short) 3000));
    assertThat((Short) uint32ToInt16.invoke(null, 0x40000), equalTo((short) 0));

    Method uint32ToUint16 = clazz.getMethod("uint32ToUint16", int.class);
    assertThat((Character) uint32ToUint16.invoke(null, 0), equalTo((char) 0));
    assertThat((Character) uint32ToUint16.invoke(null, 0x402), equalTo((char) 1026));
    assertThat((Character) uint32ToUint16.invoke(null, 0x1000), equalTo((char) 4096));
    assertThat((Character) uint32ToUint16.invoke(null, 0xFFFF), equalTo((char) 65535));
    assertThat((Character) uint32ToUint16.invoke(null, 0x10003), equalTo((char) 3));

    Method uint32ToUint64 = clazz.getMethod("uint32ToUint64", int.class);
    assertThat((Long) uint32ToUint64.invoke(null, 0), equalTo((long) 0));
    assertThat((Long) uint32ToUint64.invoke(null, 0xFF), equalTo(0xFFL));
    assertThat((Long) uint32ToUint64.invoke(null, 0xFFFFFFFF), equalTo(0xFFFFFFFFL));

    Method uint16ToUint64 = clazz.getMethod("uint32ToUint64", int.class);
    assertThat((Long) uint16ToUint64.invoke(null, 0), equalTo((long) 0));
    assertThat((Long) uint16ToUint64.invoke(null, 0xFF), equalTo(0xFFL));
    assertThat((Long) uint16ToUint64.invoke(null, 0xFFFFFFFF), equalTo(0xFFFFFFFFL));
  }

  @Test
  public void memcmpTest() throws Exception {
    Class clazz = compile("memcmp.c");

    Method long_memcmp = clazz.getMethod("long_memcmp", LongPtr.class, LongPtr.class);

    assertThat((Integer) long_memcmp.invoke(null, new LongPtr(0xFFFFFFFFFFFFFFFFL), new LongPtr(0xFFFL)), greaterThan(0));
    assertThat((Integer) long_memcmp.invoke(null, new LongPtr(0xCAFEBABE), new LongPtr(0xCAFEBABE)), equalTo(0));
  }

  @Test
  public void longDouble() throws Exception {
    compileAndTest("long_double.c");
  }

  @Test
  public void voidPointers() throws Exception {
    Class clazz = compile("void_ptr.c");

    assertThat((Double) clazz.getMethod("test").invoke(null), equalTo(42.0));
    assertThat((Double) clazz.getMethod("test_from_void").invoke(null), equalTo(1.0));
  }

  @Test
  public void voidMemcmp() throws Exception {
    Class clazz = compile("void_memcmp.c");

    assertThat((Integer) clazz.getMethod("test_double").invoke(null), equalTo(1));
    assertThat((Integer) clazz.getMethod("test_integer").invoke(null), equalTo(0));
    assertThat((Integer) clazz.getMethod("test_offset").invoke(null), equalTo(1));
    assertThat((Integer) clazz.getMethod("test_comparison").invoke(null), equalTo(0));
  }

  @Test
  public void voidMalloc() throws Exception {
    compileAndTest("void_malloc.c");
  }

  @Test
  public void div() throws Exception {
    compileAndTest("div.c");
  }

  @Test
  public void stdVector() throws Exception {
    compileAndTest("std_vector.cpp");
  }

  @Test
  public void clockTest() throws Exception {
    compileAndTest("clock.c");
  }

  @Test
  public void endpointClass() throws Exception {
    // Plugin was segfaulting 
    Class<?> endpoints = compile("endpoint.cpp");

    Method allocMethod = endpoints.getMethod("alloc_endpoints");
    Ptr ptr = (Ptr) allocMethod.invoke(null);

//    assertThat(ptr.array.length, equalTo(2));


    Method testMethod = endpoints.getMethod("test_endpoints");
    Ptr ep = (Ptr) testMethod.invoke(null);
   // assertThat(ep.array.length, equalTo(8));
  }

  @Test
  public void recordValues() throws Exception {
    compileAndTest("record_value.c");
  }

  @Test
  public void clz() throws Exception {
    compileAndTest("clz.c");
  }

  @Test
  public void voidEq() throws Exception {
    compileAndTest("void_eq.c");
  }

  @Test
  public void addressablePointerUnion() throws Exception {
    compileAndTest("addr_ptr_union.c");
  }

  @Test
  public void superClass() throws Exception {
    compileAndTest("superclass.c");
  }

  @Test
  public void linkedList() throws Exception {
    compileAndTest("linked_list.c");
  }

  @Test
  public void triplePtr() throws Exception {
    compileAndTest("triple_ptr.c");
  }

  @Test
  public void print() throws Exception {
    compileAndTest("print.c");
  }

  @Test
  public void bitfields() throws Exception {
    compileAndTest("bitfields.c");
  }

  @Test
  public void unionArrayPrimitiveMixed() throws Exception {
    compileAndTest("union_arrays1.c");
  }


  @Test
  public void unionArrayPrimitiveMixed2() throws Exception {
    compileAndTest("union_arrays2.c");
  }
  
  @Test
  public void fortranCommonBlocks() throws Exception {
    Class<?> commonBlocks = compile("common.f");

    Method sub1 = commonBlocks.getMethod("sub1_");
    
    sub1.invoke(null);
  }

  @Test
  public void funptrFields() throws Exception {
    compileAndTest("funptr_fields.c");
  }
  
  @Test
  public void recordArrayFields() throws Exception {
    compileAndTest("record_array_fields.c");    
  }
  
  @Test
  public void voidPtrSet() throws Exception {
    compileAndTest("void_ptr_set.c");
  }
 
  @Test
  public void addressableFieldMemSet() throws Exception {
    compileAndTest("addr_field_memset.c");
  }
  
  @Test
  public void zeroLengthArrayFields() throws Exception {
    compileAndTest("zero_array_fields.c");
  }
  
  @Test
  public void fatPtrFieldParamAssign() throws Exception {
    compileAndTest("fatptrfield_param_assign.c");
  }


  @Test
  public void tukey() throws Exception {
    compile("dt.c");
  }

  @Test
  public void constantStaticVar() throws Exception {
    compileAndTest("staticvar.c");
  }

  @Test
  public void deferenceToUnitRecord() throws Exception {
    compileAndTest("void_deref_to_unit_rec.c");
  }

  @Test
  public void nullWrappedPtr() throws Exception {
    compileAndTest("null_ptr.c");
  }

  @Test
  public void voidRealloc() throws Exception {
    compileAndTest("void_realloc.c");
  }

  @Test
  public void voidPtrToRecordPtrParam() throws Exception {
    compileAndTest("void_to_rec_ptr_param.c");
  }

  @Test
  public void firstFirstFieldCast() throws Exception {
    compileAndTest("first_field.c");
  }

  @Test
  public void superClassBug() throws Exception {
    compileAndTest("superclass_bug.c");
  }

  @Test
  public void newArray() throws Exception {
    compileAndTest("new_array.cpp");
  }

  @Test
  public void emptyFunctions() throws Exception {
    compileAndTest("empty_fns.cpp");
  }

  @Test
  public void pointerMemberFunctions() throws Exception {
    compileAndTest("pmf.cpp");
  }

  @Test
  public void cacheBug() throws Exception {
    compileAndTest("cache_bug.c");
  }

  @Test
  public void matrixCompileBug() throws Exception {
    compile("matrix_bug.c");
  }

  @Test
  public void emptyRecordCopy() throws Exception {
    compile("empty_record_copy.cpp");
  }

  @Test
  public void strcpy() throws Exception {
    compileAndTest("strcpy.c");
  }

  @Test
  public void unitPtrSuperClass() throws Exception {
    compileAndTest("unit_ptr_superclass.c");
  }

  @Test
  public void addrRecordParam() throws Exception {
    compileAndTest("addr_record_param.c");
  }

  @Test
  public void emptyBaseClassWithRecordArray() throws Exception {
    compileAndTest("empty_base_array.cpp");
  }

  @Test
  public void emptyBaseClassWithObjectPtr() throws Exception {
    compileAndTest("empty_base_objectptr.cpp");
  }

  @Test
  public void emptyBaseClassWithUnitPtr() throws Exception {
    compileAndTest("empty_base_unitptr.cpp");
  }

  @Test
  public void addressFatPtr() throws Exception {
    compileAndTest("address_fatptr.c");
  }

  @Test
  public void differentTypesOfUnionPtr() throws Exception {
    compileAndTest("unionptr_different_types.c");
  }

  @Test
  public void addressableParameter() throws Exception {
    compileAndTest("addr_parameter.c");
  }

  @Test
  public void inheritanceGlobalVars() throws Exception {
    compileAndTest("inheritance_globalvars.cpp");
  }

  @Test
  public void badArrayLiteral() throws Exception {
    compileAndTest("bad_array_literal.c");
  }

  @Test
  public void longArrayIndex() throws Exception {
    compileAndTest("long_index.c");
  }

  @Test
  public void downcast64() throws Exception {
    compileAndTest("downcast64.c");
  }

  @Test
  public void inlineAssembly() throws Exception {
    Class<?> clazz = compile("asm.c");

    Method testMethod = clazz.getMethod("test_asm");

    try {
      testMethod.invoke(null);
      throw new AssertionError("Should not complete - there's inline assembly!");
    } catch (InvocationTargetException e) {
      assertThat(e.getCause().getMessage(), equalTo("Compilation of inline assembly not supported"));
    }
  }

  @Test
  public void arraysOfMixedLength() throws Exception {
    compileAndTest("array_mixed_len_union.c");
  }

  @Test
  public void unsignedDiv() throws Exception {
    compileAndTest("unsigned_div.c");
  }

  @Test
  public void realPointerCasting() throws Exception {
    compileAndTest("pointer_casting.c");
  }
}
