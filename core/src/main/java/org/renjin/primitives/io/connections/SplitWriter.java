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
package org.renjin.primitives.io.connections;

import java.io.IOException;
import java.io.Writer;

/**
 * Writer which delegates to two different Writers.
 */
class SplitWriter extends Writer {

  private Writer writer1;
  private Writer writer2;

  public SplitWriter(Writer writer1, Writer writer2) {
    this.writer1 = writer1;
    this.writer2 = writer2;
  }

  @Override
  public void write(char[] chars, int off, int len) throws IOException {
    writer1.write(chars, off, len);
    writer2.write(chars, off, len);
  }

  @Override
  public void flush() throws IOException {
    writer1.flush();
    writer2.flush();
  }

  @Override
  public void close() throws IOException {
    writer1.close();
    writer2.close();
  }
}
