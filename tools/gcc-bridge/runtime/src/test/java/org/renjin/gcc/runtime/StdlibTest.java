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
package org.renjin.gcc.runtime;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.gcc.runtime.BytePtr.NULL;

public class StdlibTest {

  @Test
  public void ctime() {
    IntPtr time = new IntPtr(0);
    Stdlib.time(time);
    BytePtr str = Stdlib.ctime(time);

    System.out.println(str.nullTerminatedString());
  }

  @Test
  public void lroundf() {

    assertThat(Stdlib.lroundf( 2.3f), equalTo(2L));
    assertThat(Stdlib.lroundf( 2.5f), equalTo(3L));
    assertThat(Stdlib.lroundf( 2.7f), equalTo(3L));
    assertThat(Stdlib.lroundf(-2.3f), equalTo(-2L));
    assertThat(Stdlib.lroundf(-2.5f), equalTo(-3L));
    assertThat(Stdlib.lroundf(-2.7f), equalTo(-3L));
    assertThat(Stdlib.lroundf(-0), equalTo(0L));
    assertThat(Stdlib.lroundf(Float.NEGATIVE_INFINITY), equalTo(Long.MIN_VALUE));
    assertThat(Stdlib.lroundf(Float.POSITIVE_INFINITY), equalTo(Long.MAX_VALUE));

    assertThat(Stdlib.lroundf(Long.MAX_VALUE+1.5f), equalTo(Long.MAX_VALUE));
  }

  @Test
  public void strtol() {
    assertThat(Stdlib.strtol(cstring("  13"), NULL, 0), equalTo(13L));
    assertThat(Stdlib.strtol(cstring(" 42  "), NULL, 10), equalTo(42L));
    assertThat(Stdlib.strtol(cstring(" CAFE"), NULL, 16), equalTo(0xCAFEL));
    assertThat(Stdlib.strtol(cstring("0xCAFE  "), NULL, 16), equalTo(0xCAFEL));
    assertThat(Stdlib.strtol(cstring("0xCAFE  "), NULL, 0), equalTo(0xCAFEL));
    assertThat(Stdlib.strtol(cstring("0600 the fo"), NULL, 8), equalTo(0600L));
    assertThat(Stdlib.strtol(cstring("-33405"), NULL, 10), equalTo(-33405L));
    assertThat(Stdlib.strtol(cstring("+63423"), NULL, 10), equalTo(+63423L));
  }

  @Test
  public void strtoul() {
    assertThat(Stdlib.strtoul(cstring("18446744073709551615"), NULL, 0), equalTo(0xffffffffffffffffL));
    assertThat(Stdlib.strtoul(cstring("18446744073709551614"), NULL, 0), equalTo(0xfffffffffffffffeL));
    assertThat(Stdlib.strtoul(cstring("0xfffffffffffffffe"), NULL, 0), equalTo(0xfffffffffffffffeL));
  }

  @Test
  public void strcspn() {
    Ptr x = BytePtr.nullTerminatedString("hello world", StandardCharsets.US_ASCII);
    Ptr y = BytePtr.nullTerminatedString("MXo", StandardCharsets.US_ASCII);
    Ptr z = BytePtr.nullTerminatedString("QP", StandardCharsets.US_ASCII);

    assertThat(Stdlib.strcspn(x, y), equalTo(4));
    assertThat(Stdlib.strcspn(x, z), equalTo(Stdlib.strlen(x)));
  }

  private BytePtr cstring(String str) {
    return BytePtr.nullTerminatedString(str, StandardCharsets.UTF_8);
  }

  @Test
  public void timeofday() {
    IntPtr time = new IntPtr(new int[2]);
    IntPtr timezone = new IntPtr(new int[2]);
    Stdlib.gettimeofday(time, timezone);

    System.out.println("t = " + Arrays.toString(time.array));

  }

}