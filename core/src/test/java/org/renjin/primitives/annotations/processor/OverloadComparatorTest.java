package org.renjin.primitives.annotations.processor;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.renjin.primitives.Ops;
import org.renjin.primitives.Types;
import org.renjin.primitives.subset.Subsetting;
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
    JvmMethod vectorMethod = new JvmMethod(Types.class.getMethod("asVector", Vector.class, String.class));
    JvmMethod pairListMethod = new JvmMethod(Types.class.getMethod("asVector", PairList.class, String.class));

    OverloadComparator comparator = new OverloadComparator();

    assertThat(comparator.compare(vectorMethod, pairListMethod), lessThan(0));
  }

  @Test
  public void subset2() throws Exception {
     JvmMethod stringMethod = new JvmMethod(Subsetting.class.getMethod("getSingleElementDefaultByExactName", Vector.class, String.class));
    JvmMethod intMethod = new JvmMethod(Subsetting.class.getMethod("getSingleElementDefault", Vector.class, int.class));

    OverloadComparator comparator = new OverloadComparator();

    assertThat(comparator.compare(intMethod, stringMethod), lessThan(0));

  }
}
