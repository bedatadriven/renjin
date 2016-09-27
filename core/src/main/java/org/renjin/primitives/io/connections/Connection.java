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
 *  By default the connection is not opened when created (except for ‘socketConnection’), but may
 *  be opened by setting a non-empty value of argument ‘open’.
 *
 */
public interface Connection {
  
  enum Type {
    TEXT,
    BINARY
  };
  
  void open(OpenSpec spec) throws IOException;
  
  /**
   * 
   * @return the file 
   * @throws IOException 
   */
  InputStream getInputStream() throws IOException;
  
  PushbackBufferedReader getReader() throws IOException;
  
  OutputStream getOutputStream() throws IOException;

  PrintWriter getPrintWriter() throws IOException;
  
  void close() throws IOException;
  
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
  
  Type getType();
}
