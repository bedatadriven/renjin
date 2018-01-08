/**
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
package org.renjin.gcc.runtime;

import java.io.IOException;


public interface FileHandle {

  /**
   * Beginning of file
   */
  int SEEK_SET = 0;
  /**
   * Current position of the file pointer
   */
  int SEEK_CURRENT = 1;
  /**
   * SEEK_END
   */
  int SEEK_END = 2;



  int read() throws IOException;

  void write(int b) throws IOException;

  void flush() throws IOException;

  void close() throws IOException;

  void seekSet(long offset) throws IOException;

  void seekCurrent(long offset) throws IOException;

  void seekEnd(long offset);
}
