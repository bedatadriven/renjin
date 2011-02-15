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

package r.base.regex;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RETest {

  @Test
  public void posixClasses() {
    // R regexps use double brackets for posix character classes
    assertTrue( new RE("^[[:digit:]]+$").match("1249234") );
  }

  @Test
  public void dashInCharacterClass() {

    // in these cases, R treats the hyphen as a literal
    assertTrue( new RE("^[a-]+$").match("a-a--aa---a-aaa") );
    assertTrue( new RE("^[-a]+$").match("a-a--aa---a-aaa") );
    assertTrue( new RE("^[-3]+$").match("3-3-333---3") );
    assertFalse(new RE("^[-3]+$").match("23"));

    // make sure that normal character ranges still work
    assertTrue(new RE("^[a-z4]+$").match("qf444ee"));
  }


}
