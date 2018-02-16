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

package org.renjin.primitives.io.scan;

import org.renjin.primitives.io.connections.ByteProcessor;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.nio.ByteBuffer;

public class ListScanner implements ByteProcessor, Scanner {

  private static final int COLUMN_BUFFER_SIZE = 1024;

  private AtomicReader[] readers;
  private final byte fieldSeparator;
  private final byte quoteChar;

  private final byte[] columnBuffer;

  public ListScanner(AtomicReader[] readers, byte separator, byte quoteChar) {
    this.readers = readers;
    this.fieldSeparator = separator;
    this.quoteChar = quoteChar;
    this.columnBuffer = new byte[COLUMN_BUFFER_SIZE];
  }

  @Override
  public void process(ByteBuffer buffer, boolean endOfInput) {
    int remaining = buffer.remaining();
    int column = 0;
    boolean quoted = false;

    byte[] columnBuffer = this.columnBuffer;
    int columnPos = 0;

    for (int i = 0; i < remaining; i++) {
      byte b = buffer.get(i);

      boolean newLine = (b == (byte)'\n');
      boolean newColumn = (b == fieldSeparator);
      boolean endOfColumn = newLine || newColumn;

      if(endOfColumn) {
        int columnLength = columnPos;
        if(newLine && i > 0 && buffer.get(i-1) == (byte)'\r') {
          columnLength --;
        }
        readers[column].readUTF8(columnBuffer, columnLength);
        column ++;
        columnPos = 0;

        if(newLine) {
          column = 0;
        }
      } else {
        columnBuffer[columnPos++] = b;
      }
    }

    if(endOfInput) {
      readers[column].readUTF8(columnBuffer, columnPos);
    }

  }

  @Override
  public ListVector build() {
    SEXP[] columns = new SEXP[readers.length];
    for (int i = 0; i < readers.length; i++) {
      columns[i] = readers[i].build();
    }
    return new ListVector(columns);
  }
}
