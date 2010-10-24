/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.parser;

import java.util.ArrayList;

class FunctionSourceBuffer {

  private static final int MAX_NEST = 256;
  private static final int MAXFUNSIZE = 131072;

  private StringBuffer source = new StringBuffer();

  private int level = 0;

  private ArrayList<Integer> startIndices = new ArrayList<Integer>();

  /**
   * descends into a new function
   */
  public void descend() {
    if (level >= MAX_NEST) {
      throw new RLexException("functions nested too deeply in source code at line %d");
    }
    if (level++ == 0) {
      source.append("function");
    }
    startIndices.add(source.length() - 8);
  }

  public void maybeAppendSourceCodePoint(int c) {
    if (level > 0) {
      if (source.length() > MAXFUNSIZE) {
        throw new RLexException("function is too long to keep source");
      }
    }
  }

  public void ascend() {
    level--;
  }
}
