/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc;

import org.renjin.gcc.runtime.AbstractFileHandle;

import java.io.IOException;
import java.io.PrintStream;

public class StdOutHandle extends AbstractFileHandle {

  private final PrintStream out;

  public StdOutHandle(PrintStream out) {
    this.out = out;
  }

  @Override
  public int read() throws IOException {
    throw new UnsupportedOperationException("Cannot read from stdout");
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
  }

  @Override
  public void rewind() throws IOException {
    throw new UnsupportedOperationException("Cannot rewind stdout");
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public void seekSet(long offset) throws IOException {
    throw new UnsupportedOperationException("Cannot seek stdout");
  }

  @Override
  public void seekCurrent(long offset) throws IOException {
    throw new UnsupportedOperationException("Cannot seek stdout");
  }

  @Override
  public void seekEnd(long offset) {
    throw new UnsupportedOperationException("Cannot seek stdout");
  }
}
