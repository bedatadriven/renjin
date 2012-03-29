package org.renjin.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.renjin.sexp.StringVector;
import org.renjin.util.NamesBuilder;


public class NamesBuilderTest {

  @Test
  public void build() {
    NamesBuilder builder = NamesBuilder.withInitialLength(3);
    assertThat(builder.haveNames(), equalTo(false));
    
    builder.set(0, StringVector.NA);
    builder.set(1, StringVector.NA);
    assertThat(builder.haveNames(), equalTo(false));

    builder.add("Foo");
    assertThat(builder.haveNames(), equalTo(true));
    
  }
  
}
