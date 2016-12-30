/*
 * Renjin Regular Expression Library, based on gnu-regexp
 * Copyright (C) 1998-2001 Wes Biggs
 * Copyright (C) 2016 BeDataDriven Groep BV
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.renjin.regexp;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.StringBufferInputStream;
import java.io.StringReader;

import static org.junit.Assert.assertThat;

public class RexpTest {


  private static void check(RE expr, String input, String expect, int id) {
    System.out.print("Test #" + id + ": ");
    // Test it using all possible input types
    check(expr.getMatch(input), expect, "String");
    check(expr.getMatch(new StringBuffer(input)), expect, "StringBuffer");
    check(expr.getMatch(input.toCharArray()), expect, "char[]");
    check(expr.getMatch(new StringReader(input)), expect, "Reader");
    check(expr.getMatch(new StringBufferInputStream(input)), expect, "InputStream");
    System.out.println();
  }

  private static void check(REMatch m, String expect, String type) {
    boolean passed;
    if (m == null) {
      passed = (expect != null);
    } else {
      passed = (m.toString().equals(expect));
    }
    System.out.print("[" + type + " ");
    System.out.print(passed ? "OK]" : " *** FAILED] ");
  }


  @Test
  public void test() throws REException {
    RE e;

    e = new RE("(.*)z");
    check(e, ("xxz"), "xxz", 1);

    e = new RE(".*z");
    check(e, ("xxz"), "xxz", 2);

    e = new RE("(x|xy)z");
    check(e, ("xz"), "xz", 3);
    check(e, ("xyz"), "xyz", 4);

    e = new RE("(x)+z");
    check(e, ("xxz"), "xxz", 5);

    e = new RE("abc");
    check(e, ("xyzabcdef"), "abc", 6);

    e = new RE("^start.*end$");
    check(e, ("start here and go to the end"), "start here and go to the end", 7);

    e = new RE("(x|xy)+z");
    check(e, ("xxyz"), "xxyz", 8);

    e = new RE("type=([^ \t]+)[ \t]+exts=([^ \t\n\r]+)");
    check(e, ("type=text/html	exts=htm,html"), "type=text/html	exts=htm,html", 9);

    e = new RE("(x)\\1");
    check(e, ("zxxz"), "xx", 10);

    e = new RE("(x*)(y)\\2\\1");
    check(e, ("xxxyyxx"), "xxyyxx", 11);

    e = new RE("[-go]+");
    check(e, ("go-go"), "go-go", 12);

    e = new RE("[\\w-]+");
    check(e, ("go-go"), "go-go", 13);

    e = new RE("^start.*?end");
    check(e, ("start here and end in the middle, not the very end"), "start here and end", 14);

    e = new RE("\\d\\s\\w\\n\\r");
    check(e, ("  9\tX\n\r  "), "9\tX\n\r", 15);

    e = new RE("zow", RE.REG_ICASE);
    check(e, ("ZoW"), "ZoW", 16);

    e = new RE("(\\d+)\\D*(\\d+)\\D*(\\d)+");
    check(e, ("size--10 by 20 by 30 feet"), "10 by 20 by 30", 17);

    e = new RE("(ab)(.*?)(d)");
    REMatch m = e.getMatch("abcd");
    System.out.print("Test #18: ");
    check(m, "abcd", "String");
    System.out.println(((m.toString(2).equals("c")) ? "Pass" : "*** Fail")
        + "ed test #19");

    e = new RE("^$");
    check(e, (""), "", 20);

    e = new RE("a*");
    check(e, (""), "", 21);
    check(e, ("a"), "a", 22);
    check(e, ("aa"), "aa", 23);

    e = new RE("(([12]))?");
    check(e, ("12"), "1", 24);

    e = new RE("(.*)?b");
    check(e, ("ab"), "ab", 25);

    e = new RE("(.*)?-(.*)");
    check(e, ("a-b"), "a-b", 26);

    e = new RE("(a)b");
    check(e, ("aab"), "ab", 27);

    e = new RE("[M]iss");
    check(e, ("one Mississippi"), "Miss", 28);

    e = new RE("\\S Miss");
    check(e, ("one Mississippi"), "e Miss", 29);

    e = new RE("a*");
    check(e, ("b"), "", 30);
    check(e, ("ab"), "a", 31);
    check(e, ("aab"), "aa", 32);

    // Single character should match anywhere in String
    e = new RE("a");
    check(e, ("a"), "a", 33);
    check(e, ("ab"), "a", 34);
    check(e, ("ba"), "a", 35);
    check(e, ("bab"), "a", 36);

    // End-of-line should match in any case
    e = new RE("$");
    check(e, (""), "", 37);
    check(e, ("a"), "", 38);
    check(e, ("abcd"), "", 39);
  }
}
