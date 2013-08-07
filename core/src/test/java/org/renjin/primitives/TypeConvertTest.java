package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Logical;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TypeConvertTest extends EvalTestCase {
  
  @Test
  public void convert() {
    assertThat(eval(".Internal(type.convert(c('1','2','3'), 'NA', FALSE, '.'))"), equalTo(c_i(1,2,3)));
    assertThat(eval(".Internal(type.convert(c('T','NA','F'), 'NA', FALSE, '.'))"), equalTo(c(Logical.TRUE, Logical.NA, Logical.FALSE)));
    assertThat(eval(".Internal(type.convert(c('T','NA',''), 'NA', FALSE, '.'))"), equalTo(c(Logical.TRUE, Logical.NA, Logical.NA)));
    assertThat(eval(".Internal(type.convert(c('T','FALSE','BOB'), 'BOB', FALSE, '.'))"), equalTo(c(Logical.TRUE, Logical.FALSE, Logical.NA)));
    assertThat(eval(".Internal(type.convert(c('3.5','3.6','FOO'), 'FOO', FALSE, '.'))"), equalTo(c(3.5,3.6,DoubleVector.NA)));
    assertThat(eval(".Internal(type.convert(c('bing', 'bop'), 'FOO', TRUE, '.'))"), equalTo(c("bing","bop")));
    assertThat(eval(".Internal(type.convert(c('bing', 'bop'), 'FOO', FALSE, '.'))"), equalTo(c_i(1,2)));

  }
  
}
