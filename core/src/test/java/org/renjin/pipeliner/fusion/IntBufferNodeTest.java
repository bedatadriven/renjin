/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.pipeliner.fusion;

import org.junit.Test;
import org.renjin.pipeliner.VectorPipeliner;
import org.renjin.primitives.summary.DeferredMean;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntBufferVector;
import org.renjin.sexp.Vector;

import java.nio.IntBuffer;
import java.util.concurrent.Executors;

public class IntBufferNodeTest {

  @Test
  public void test() {
    int[] array = new int[100];
    for(int i=0;i<array.length;++i) {
      array[i] = i;
    }
    IntBuffer buffer = IntBuffer.wrap(array);
    IntBufferVector vector = new IntBufferVector(buffer, array.length);

    DeferredMean sum = new DeferredMean(vector, AttributeMap.EMPTY);

    VectorPipeliner pipeliner = new VectorPipeliner(Executors.newFixedThreadPool(1));
    Vector result = pipeliner.materialize(sum);

    System.out.println(result);
  }

}