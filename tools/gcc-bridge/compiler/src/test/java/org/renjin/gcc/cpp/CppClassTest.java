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
package org.renjin.gcc.cpp;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CppClassTest extends AbstractGccCppTest {

  @Test
  public void createClass() throws Exception {
    compile("cppclass.cpp");

    Class<?> clazz = Class.forName("org.renjin.gcc.cppclass");
    Method method = clazz.getMethod("create");

    Object rect = method.invoke(null);
    assertThat(rect, is(not(nullValue())));
  }

  @Test
  public void constructors() throws Exception {
    Class<?> clazz = compile("constructors.cpp");

    Method method = clazz.getMethod("run");
    Integer retval = (Integer) method.invoke(null);
    assertThat(retval, is(Integer.valueOf(5)));
  }

  @Test
  public void destructors() throws Exception {
    Class<?> clazz = compile("destructors.cpp");

    Method method = clazz.getMethod("run");
    Integer retval = (Integer) method.invoke(null);
    assertThat(retval, is(Integer.valueOf(3)));
  }
}