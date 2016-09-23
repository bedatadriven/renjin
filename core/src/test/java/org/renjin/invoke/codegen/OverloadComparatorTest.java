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
package org.renjin.invoke.codegen;

import org.junit.Test;
import org.renjin.eval.Context;
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
    JvmMethod pairListMethod = new JvmMethod(Vectors.class.getMethod("asVector", Context.class, PairList.class, String.class));

    OverloadComparator comparator = new OverloadComparator();

    assertThat(comparator.compare(vectorMethod, pairListMethod), lessThan(0));
  }
}
