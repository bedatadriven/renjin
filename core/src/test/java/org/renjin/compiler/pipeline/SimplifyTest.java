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
package org.renjin.compiler.pipeline;


import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.primitives.matrix.TransposingMatrix;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import static org.junit.Assert.assertThat;

public class SimplifyTest extends EvalTestCase {

  @Test
  public void simplificationPreservesAttributes() {

    
    Vector x = new DoubleSequence(AttributeMap.builder().setDim(200,40).build(), 1, 1, 8000);
    TransposingMatrix xt = new TransposingMatrix(x, AttributeMap.builder().setDim(40,200).build());
    
    SimpleVectorPipeliner pipeliner = new SimpleVectorPipeliner();
    
    Vector xts = pipeliner.simplify(xt);
    
    assertThat(xts.getAttribute(Symbols.DIM), elementsIdenticalTo(c_i(40,200)));
  }

}
