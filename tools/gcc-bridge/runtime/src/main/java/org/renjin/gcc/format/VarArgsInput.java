/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.gcc.format;

import org.renjin.gcc.runtime.*;

public class VarArgsInput implements FormatInput {
  private Formatter formatter;
  private Ptr argumentList;
  private int offsets[];

  public VarArgsInput(Formatter formatter, Ptr argumentList) {
    this.formatter = formatter;
    this.argumentList = argumentList;
    this.offsets = new int[formatter.getArgumentTypes().size()];

    int offset = 0;
    for (int i = 0; i < offsets.length ; i++) {
      offsets[i] = offset;
      switch (formatter.getArgumentType(i)) {
        case INTEGER:
        case POINTER:
        case STRING:
          offset += IntPtr.BYTES;
          break;
        case DOUBLE:
          offset += DoublePtr.BYTES;
          break;
        case UNUSED:
          break;
        case LONG:
          offset += LongPtr.BYTES;
          break;
      }
    }
  }


  @Override
  public boolean isNA(int argumentIndex) {
    return false;
  }

  @Override
  public int getInt(int argumentIndex) {
    return argumentList.getInt(offsets[argumentIndex]);
  }

  @Override
  public long getLong(int argumentIndex) {
    return argumentList.getLong(offsets[argumentIndex]);
  }

  @Override
  public long getUnsignedLong(int argumentIndex) {
    return argumentList.getLong(offsets[argumentIndex]);
  }

  @Override
  public double getDouble(int argumentIndex) {
    return argumentList.getDouble(offsets[argumentIndex]);
  }

  @Override
  public String getString(int argumentIndex) {
    return Stdlib.nullTerminatedString(argumentList.getPointer(offsets[argumentIndex]));
  }
}

