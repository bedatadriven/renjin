package org.renjin.compiler.pipeline.specialization;

import org.junit.Test;
import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.primitives.R$primitive$$times$deferred_dd;
import org.renjin.primitives.R$primitive$$times$deferred_ii;
import org.renjin.primitives.matrix.DeferredColSums;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.sexp.*;

import java.nio.IntBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class ColSumSpecializerTest {

  @Test
  public void simpleTest() throws Exception {


    //        [,1] [,2] [,3]
    //  [1,]    1    5    9
    //  [2,]    2    6   10
    //  [3,]    3    7   11
    //  [4,]    4    8   12
    DeferredColSums colSums = new DeferredColSums(new IntSequence(1, 1, 12), 3, false, AttributeMap.EMPTY);

    DeferredGraph graph = new DeferredGraph(colSums);

    JitSpecializer specializer = new JitSpecializer();
    SpecializedComputation computation = specializer.compile(graph.getRoot());

    double[] resultArray = computation.compute(graph.getRoot().flattenVectors());

    System.out.println(Arrays.toString(resultArray));

    assertArrayEquals(new double[]{10d, 26d, 42d}, resultArray, 0.1);
  }

  @Test
  public void missingValues() throws Exception {


    //        [,1] [,2] [,3]
    //  [1,]    1    5   NA
    //  [2,]    2    6   10

    IntArrayVector matrix = new IntArrayVector(1, 2, 5, 6, IntVector.NA, 10);

    DeferredColSums colSums = new DeferredColSums(matrix, 3, false, AttributeMap.EMPTY);

    DeferredGraph graph = new DeferredGraph(colSums);

    JitSpecializer specializer = new JitSpecializer();
    SpecializedComputation computation = specializer.compile(graph.getRoot());

    double[] resultArray = computation.compute(graph.getRoot().flattenVectors());

    System.out.println(Arrays.toString(resultArray));

    assertArrayEquals(new double[]{3d, 11d, 10d}, resultArray, 0.1);
  }


  @Test
  public void singleColumn() throws Exception {

    IntBufferVector x = new IntBufferVector(IntBuffer.wrap(new int[] { 1, 2, 3 }), 3);
    IntBufferVector y = new IntBufferVector(IntBuffer.wrap(new int[] { 4, 5, 6 }), 3);
    AtomicVector times = new R$primitive$$times$deferred_ii(x, y, AttributeMap.EMPTY);

    DeferredColSums colSums = new DeferredColSums(times, 1, false, AttributeMap.EMPTY);

    DeferredGraph graph = new DeferredGraph(colSums);

    JitSpecializer specializer = new JitSpecializer();
    SpecializedComputation computation = specializer.compile(graph.getRoot());

    double[] resultArray = computation.compute(graph.getRoot().flattenVectors());

    System.out.println(Arrays.toString(resultArray));

    assertArrayEquals(new double[]{32d }, resultArray, 0.1);
  }
}