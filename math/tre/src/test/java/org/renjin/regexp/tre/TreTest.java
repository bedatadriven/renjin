/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.regexp.tre;

import org.junit.Test;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.IntPtr;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TreTest {

  @Test
  public void basicTest() {

    regex_t  regex = new regex_t();
    regcomp.tre_regcomp(regex, BytePtr.nullTerminatedString("^foo+", StandardCharsets.US_ASCII), 1);

    IntPtr matches = new IntPtr(new int[4]);
    int result = regexec.tre_regexec(regex, BytePtr.nullTerminatedString("fooooo", StandardCharsets.US_ASCII), 1, matches, 0);

    System.out.println("result = " + result);
    System.out.println("matches = " + Arrays.toString(matches.array));

    assertThat(result, equalTo(0));
    assertThat(matches.array[0], equalTo(0));
    assertThat(matches.array[1], equalTo(6));
  }
}
