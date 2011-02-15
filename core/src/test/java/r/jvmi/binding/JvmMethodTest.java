/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.jvmi.binding;

import org.junit.Test;
import r.lang.AtomicVector;
import r.lang.Null;
import r.lang.exception.EvalException;

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
