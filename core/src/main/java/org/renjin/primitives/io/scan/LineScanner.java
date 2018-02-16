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

import org.renjin.primitives.io.connections.CharProcessor;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads in complete lines.
 */
public class LineScanner implements CharProcessor {

  private String leftover = null;

  private final List<String> lines = new ArrayList<>();

  @Override
  public void process(char[] buffer, int bufferLength, boolean endOfInput) {
    String leftover = this.leftover;
    int lastLineStart = 0;

    for (int i = 0; i < bufferLength; i++) {
      char c = buffer[i];
      if( c == '\n' ) {
        String line = toLine(buffer, lastLineStart, i);
        if(leftover != null) {
          line = leftover + line;
          leftover = null;
        }
        lines.add(line);
        lastLineStart = i + 1;
      }
    }

    leftover = toLine(buffer, lastLineStart, bufferLength);

    if(endOfInput) {
      if(!leftover.isEmpty()) {
        lines.add(leftover);
      }
    } else {
      this.leftover = leftover;
    }
  }

  private String toLine(char[] buffer, int start, int end) {
    if(start == end) {
      return "";
    }
    if(start < end && buffer[end-1] == '\r') {
      end--;
    }

    return new String(buffer, start, end - start);
  }

  public List<String> getLines() {
    return lines;
  }
}
