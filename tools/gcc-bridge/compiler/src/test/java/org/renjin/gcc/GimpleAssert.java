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
package org.renjin.gcc;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.Stdlib;

import java.util.Objects;

/**
 * Methods to be used in tests
 */
public class GimpleAssert {

  public static void putd(double x) {
    System.out.println(x);
  }

  public static void putbx(byte x) { System.out.println(x);}

  public static void assertTrue(BytePtr message, int x) {
    if(x == 0) {
      throw new AssertionError(message.nullTerminatedString());
    }
  }

  public static void assertStringsEqual(BytePtr actualPtr, BytePtr expectedPtr) {
    String actual = Stdlib.nullTerminatedString(actualPtr);
    String expected = Stdlib.nullTerminatedString(expectedPtr);
    if(!Objects.equals(actual, expected)) {
      throw new AssertionError(String.format("Expected '%s' but result was '%s'", expected, actual));
    }
  }
}
