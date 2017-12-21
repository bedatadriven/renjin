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


import java.io.*;


/**
 * Connection object for the standard input stream.
 * Currently a stub, will throw an exception if actually used.
 */
public class StdInConnection implements Connection {

  private PushbackBufferedReader stream = new PushbackBufferedReader(new InputStreamReader(System.in));

  @Override
  public InputStream getInputStream() throws IOException {
    throw new UnsupportedOperationException("Cannot read bytes from stdin");
  }

  @Override
  public PrintWriter getPrintWriter() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter getOpenPrintWriter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws IOException {
    /* NOOP */
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public PushbackBufferedReader getReader() {
    return stream;
  }

  public void setReader(Reader reader) {
    stream = new PushbackBufferedReader(reader);
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public void flush() throws IOException {
    // NOOP
  }

  @Override
  public void open(OpenSpec spec) throws IOException {

  }

  @Override
  public String getClassName() {
    return "terminal";
  }

  @Override
  public String getDescription() {
    return "stdin";
  }

  @Override
  public String getMode() {
    return "r";
  }

  @Override
  public boolean canRead() {
    return true;
  }

  @Override
  public boolean canWrite() {
    return false;
  }

  @Override
  public Type getType() {
    return Type.TEXT;
  }
}
