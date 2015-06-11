package org.renjin.primitives.matrix;

import org.junit.Test;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DeferredColSumsTest {

    @Test
    public void test() {
        
        //        [,1] [,2] [,3]
        //  [1,]    1    5    9
        //  [2,]    2    6   10
        //  [3,]    3    7   11
        //  [4,]    4    8   12
        DeferredColSums colSums = new DeferredColSums(new IntSequence(1, 1, 12), 3, false, AttributeMap.EMPTY);
        Vector vector = colSums.forceResult();
        
        assertThat(vector.length(), equalTo(3));
        assertThat(vector.getElementAsDouble(0), equalTo(10d));
        assertThat(vector.getElementAsDouble(1), equalTo(26d));
        assertThat(vector.getElementAsDouble(2), equalTo(42d));


    }
}