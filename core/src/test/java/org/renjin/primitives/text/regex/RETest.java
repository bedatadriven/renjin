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
package org.renjin.primitives.text.regex;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RETest {

  @Test
  public void posixClasses() throws RESyntaxException {
    // R regexps use double brackets for posix character classes
    assertTrue( new ExtendedRE("^[[:digit:]]+$").match("1249234") );
  }

  @Test
  public void dashInCharacterClass() throws RESyntaxException {

    // in these cases, R treats the hyphen as a literal
    assertTrue( new ExtendedRE("^[a-]+$").match("a-a--aa---a-aaa") );
    assertTrue( new ExtendedRE("^[-a]+$").match("a-a--aa---a-aaa") );
    assertTrue( new ExtendedRE("^[-3]+$").match("3-3-333---3") );
    assertFalse(new ExtendedRE("^[-3]+$").match("23"));

    // make sure that normal character ranges still work
    assertTrue(new ExtendedRE("^[a-z4]+$").match("qf444ee"));
  }
}
