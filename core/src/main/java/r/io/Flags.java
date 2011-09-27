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

package r.io;

import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbols;

class Flags {
  private final static int IS_OBJECT_BIT_MASK = (1 << 8);
  private final static int HAS_ATTR_BIT_MASK = (1 << 9);
  private final static int HAS_TAG_BIT_MASK = (1 << 10);

  public final int type;
  public final int levels;
  public final boolean isObject;
  public final boolean hasAttributes;
  public final boolean hasTag;
  private final int flags;

  public Flags(int flags) {
    this.flags = flags;
    type = decodeType(flags);
    levels = decodeLevels(flags);
    isObject = (flags & IS_OBJECT_BIT_MASK) != 0;
    hasAttributes = (flags & HAS_ATTR_BIT_MASK) != 0;
    hasTag = (flags & HAS_TAG_BIT_MASK) != 0;
  }

  private int decodeType(int v) {
    return ((v) & 255);
  }

  private int decodeLevels(int v) {
    return ((v) >> 12);
  }

  public boolean isUTF8Encoded() {
    return (levels & SerializationFormat.UTF8_MASK) != 0;
  }

  public boolean isLatin1Encoded() {
    return (levels & SerializationFormat.LATIN1_MASK) != 0;
  }

  public int unpackRefIndex() {
    return   ((flags) >> 8);
  }

  public static int nullFlags() {
    return SerializationFormat.NILVALUE_SXP;
  }

  public static int computeFlags(SEXP exp, int type) {
    int flags = type;

    if(exp.getAttribute(Symbols.CLASS) != Null.INSTANCE) {
      flags |= IS_OBJECT_BIT_MASK;
    }
    if(exp.getAttributes() != Null.INSTANCE) {
      flags |= HAS_ATTR_BIT_MASK;
    }
    if(exp instanceof PairList.Node && ((PairList.Node) exp).hasTag()) {
      flags |= HAS_TAG_BIT_MASK;
    }
    return flags;
  }

}
