/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.annotation.processor;

import org.junit.Test;
import org.renjin.eval.EvalException;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Null;

import static java.util.Arrays.asList;

public class JvmMethodTest {


  @Test(expected = EvalException.class)
  public void validation() throws NoSuchMethodException {
    JvmMethod m0 = new JvmMethod(getClass().getMethod("doStuff", Boolean.TYPE, Boolean.TYPE, Boolean.TYPE));
    JvmMethod m1 = new JvmMethod(getClass().getMethod("doStuff", Null.class));
    JvmMethod m2 = new JvmMethod(getClass().getMethod("doStuff", Integer.TYPE));
    JvmMethod m3 = new JvmMethod(getClass().getMethod("doStuff", AtomicVector.class));
    JvmMethod m4 = new JvmMethod(getClass().getMethod("doStuff"));

    // make sure that we get an error because doStuff(AtomicExp) hides doStuff(NullExp)

    JvmMethod.validate(asList(m0, m1, m2, m3, m4));
  }



  public static void doStuff(AtomicVector x) {

  }

  public static void doStuff(Null x) {

  }

  public static void doStuff(int i) {

  }

  public static void doStuff() {

  }

  public static void doStuff(boolean a, boolean b, boolean c) {

  }


}
