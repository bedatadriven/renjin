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

package org.renjin.primitives.io.serialization;


import org.renjin.sexp.*;

class Flags {
 
  public final static int MAX_PACKED_INDEX = Integer.MAX_VALUE >> 8;
  
  private final static int IS_OBJECT_BIT_MASK = (1 << 8);
  private final static int HAS_ATTR_BIT_MASK = (1 << 9);
  private final static int HAS_TAG_BIT_MASK = (1 << 10);

  private Flags() {
  }

  public static int getType(int flags) {
    return ((flags) & 255);
  }

  public static int getLevels(int flags) {
    return ((flags) >> 12);
  }

  public static boolean hasAttributes(int flags) {
    return (flags & HAS_ATTR_BIT_MASK) == HAS_ATTR_BIT_MASK;
  }

  public static boolean hasTag(int flags) {
    return (flags & HAS_TAG_BIT_MASK) == HAS_TAG_BIT_MASK;
  }

  public static boolean isUTF8Encoded(int flags) {
    return (getLevels(flags) & SerializationFormat.UTF8_MASK) == SerializationFormat.UTF8_MASK;
  }

  public static boolean isLatin1Encoded(int flags) {
    return (getLevels(flags) & SerializationFormat.LATIN1_MASK) == SerializationFormat.LATIN1_MASK;
  }

  public static int unpackRefIndex(int flags) {
    return   ((flags) >> 8);
  }

  public static int computeFlags(SEXP exp, int type) {
    int flags = type;

    if(exp.getAttribute(Symbols.CLASS) != Null.INSTANCE) {
      flags |= IS_OBJECT_BIT_MASK;
    }
    if(exp.getAttributes() != AttributeMap.EMPTY) {
      flags |= HAS_ATTR_BIT_MASK;
    }
    if(exp instanceof PairList.Node && ((PairList.Node) exp).hasTag()) {
      flags |= HAS_TAG_BIT_MASK;
    }
    if(exp instanceof Closure | exp instanceof Environment) {
      flags |= HAS_TAG_BIT_MASK;
    }
    return flags;
  }
  
  public static int computePromiseFlags(Promise promise) {
    int flags = SerializationFormat.PROMSXP;

    if(promise.getAttributes() != AttributeMap.EMPTY) {
      flags |= HAS_ATTR_BIT_MASK;
    }
    if(promise.getEnvironment() != null) {
      flags |= HAS_TAG_BIT_MASK;
    }
    return flags;
  }
}
