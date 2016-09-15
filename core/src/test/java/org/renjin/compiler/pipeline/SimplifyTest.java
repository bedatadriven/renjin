package org.renjin.compiler.pipeline;


import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.primitives.matrix.TransposingMatrix;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.repackaged.guava.util.concurrent.MoreExecutors;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SimplifyTest extends EvalTestCase {

  @Test
  public void simplificationPreservesAttributes() {

    
    Vector x = new DoubleSequence(AttributeMap.builder().setDim(200,40).build(), 1, 1, 8000);
    TransposingMatrix xt = new TransposingMatrix(x, AttributeMap.builder().setDim(40,200).build());
    
    VectorPipeliner pipeliner = new VectorPipeliner(MoreExecutors.sameThreadExecutor());
    
    Vector xts = pipeliner.simplify(xt);
    
    assertThat(xts.getAttribute(Symbols.DIM), equalTo(c_i(40,200)));
  }

}
