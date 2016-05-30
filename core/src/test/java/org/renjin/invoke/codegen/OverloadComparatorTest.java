package org.renjin.invoke.codegen;

import org.junit.Test;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Ops;
import org.renjin.primitives.Vectors;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.PairList;
import org.renjin.sexp.Vector;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class OverloadComparatorTest {


  @Test
  public void scalarPriority() throws Exception {

    JvmMethod intMethod = new JvmMethod(Ops.class.getMethod("minus", int.class));
    JvmMethod doubleMethod = new JvmMethod(Ops.class.getMethod("minus", double.class));

    List<JvmMethod> list = Lists.newArrayList(doubleMethod, intMethod);

    Collections.sort(list, new OverloadComparator());

    assertThat(list.get(0), is(intMethod));
    assertThat(list.get(1), is(doubleMethod));
  }

  @Test
  public void pairListVsVector() throws Exception {
    JvmMethod vectorMethod = new JvmMethod(Vectors.class.getMethod("asVector", Vector.class, String.class));
    JvmMethod pairListMethod = new JvmMethod(Vectors.class.getMethod("asVector", PairList.class, String.class));

    OverloadComparator comparator = new OverloadComparator();

    assertThat(comparator.compare(vectorMethod, pairListMethod), lessThan(0));
  }
}
