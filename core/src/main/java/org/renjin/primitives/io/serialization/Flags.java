/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.io.serialization;


import org.renjin.sexp.*;

class Flags {


  public final static int MAX_PACKED_INDEX = Integer.MAX_VALUE >> 8;

  static final Symbol OLD_S4_BIT = Symbol.get("__S4_BIT");

  static final boolean WRITE_OLD_S4_ATTRIBUTE = true;


  private final static int IS_OBJECT_BIT_MASK = (1 << 8);
  private final static int HAS_ATTR_BIT_MASK = (1 << 9);
  private final static int HAS_TAG_BIT_MASK = (1 << 10);
  private final static int S4_BIT_MASK = (1 << 16);


  private final static int ACTIVE_BINDING_MASK = (1 << 27);


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

  public static boolean isActiveBinding(int flags) {
    return (flags & ACTIVE_BINDING_MASK) == ACTIVE_BINDING_MASK;
  }

  public static boolean isS4(int flags) {
    return (flags & S4_BIT_MASK) == S4_BIT_MASK;
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

  public static int computeBindingFlag(boolean activeBinding) {
    int flags = SexpType.LISTSXP;
    flags |= HAS_TAG_BIT_MASK;
    if (activeBinding) {
      flags |= ACTIVE_BINDING_MASK;
    }
    return flags;
  }

  public static int computeFlags(SEXP exp, int type) {
    int flags = type;

    if(exp.getAttribute(Symbols.CLASS) != Null.INSTANCE) {
      flags |= IS_OBJECT_BIT_MASK;
    }
    if(hasAttributesToWrite(exp)) {
      flags |= HAS_ATTR_BIT_MASK;
    }
    if(exp instanceof PairList.Node && ((PairList.Node) exp).hasTag()) {
      flags |= HAS_TAG_BIT_MASK;
    }
    if(exp instanceof Closure | exp instanceof Environment) {
      flags |= HAS_TAG_BIT_MASK;
    }
    if(exp.getAttributes().isS4()) {
      flags |= Flags.S4_BIT_MASK;
    }
    return flags;
  }

  private static boolean hasAttributesToWrite(SEXP exp) {
    // Previous versions of Renjin used an attribute __S4_BIT to indicate
    // that an SEXP was an S4 object. We are now correcting this representation
    // to use the same flag bit that GNU R uses, but we don't want to make it
    // impossible to load packages using S4 in older versions of Renjin, so we will
    // continue to write out the attribute until the next major version change.

    if(WRITE_OLD_S4_ATTRIBUTE) {
      return exp.getAttributes() != AttributeMap.EMPTY;
    } else {
      return exp.getAttributes().hasAnyBesidesS4Flag();
    }
  }

  public static int computePromiseFlags(Promise promise) {
    int flags = SexpType.PROMSXP;

    if(promise.getAttributes() != AttributeMap.EMPTY) {
      flags |= HAS_ATTR_BIT_MASK;
    }
    if(promise.getEnvironment() != null) {
      flags |= HAS_TAG_BIT_MASK;
    }
    return flags;
  }
  
  public static int computeCharSexpFlags(int encodingFlag) {
    return SexpType.CHARSXP | encodeLevels(encodingFlag);
  }

  private static int encodeLevels(int value) {
    return value << 12;
  }
}
