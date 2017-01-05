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
package org.renjin.primitives.io.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 *
 *
 * <p>By default the connection is not opened when created (except for ‘socketConnection’), but may
 *  be opened by setting a non-empty value of argument ‘open’.
 *
 */
public interface Connection {
  
  enum Type {
    TEXT,
    BINARY
  };

  /**
   * Opens the connection using the given open specification.
   * @param spec specifies whether to open for read or write, for text or binary.
   * @throws IOException
   */
  void open(OpenSpec spec) throws IOException;
  
  /**
   * Returns a reference to the binary input stream for this Connection.
   *
   * <p>If the connection is not yet
   * open, it will be opened in mode "rb"
   *
   * @return the InputStream for reading.
   * @throws IOException 
   */
  InputStream getInputStream() throws IOException;

  /**
   * Returns a reference to the text input reader for this Connection.
   *
   * <p>If the connection is not yet
   * open. it will be opened in mode "rt"
   *
   * @return the reader for this connection.
   * @throws IOException
   */
  PushbackBufferedReader getReader() throws IOException;

  /**
   * Returns a reference to the {@code OutputStream} for binary writing to this Connection.
   *
   * <p>If the connection is not yet
   * open, it will be opened in mode "wb".
   *
   */
  OutputStream getOutputStream() throws IOException;

  /**
   * Returns a reference to the {@code Printwriter} for text writing to this Connection.
   *
   * <p>If the connection is not
   * @return
   * @throws IOException
   */
  PrintWriter getPrintWriter() throws IOException;

  /**
   * Closes this Connection.
   * @throws IOException
   */
  void close() throws IOException;

  /**
   * @return true if this Connection has been opened and not yet closed.
   */
  boolean isOpen();
  
  /**
   * 
   * @return the S3 "sub" class of this connection (e.g. "terminal", "file", etc)
   */
  String getClassName();

  String getDescription();

  String getMode();

  boolean canRead();
  
  boolean canWrite();

  /**
   * @return the type of Connection: either {@link Type#BINARY} or {@link Type#TEXT}
   */
  Type getType();
}
