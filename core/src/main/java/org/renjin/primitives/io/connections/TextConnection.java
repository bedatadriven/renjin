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

import org.renjin.eval.EvalException;

import java.io.*;

public class TextConnection implements Connection {

  private final String objectName;
  private PushbackBufferedReader reader;
  
  public TextConnection(String objectName, String text) {
    super();
    this.objectName = objectName;
    this.reader = new PushbackBufferedReader(new StringReader(text));
  }

  @Override
  public void open(OpenSpec spec) throws IOException {
    if(!spec.forReading()) {
      throw new EvalException("Only read support for text connections is implemented, sorry!");
    }
  }

  @Override
  public InputStream getInputStream() throws IOException {
    throw new EvalException("reading bytes from TextConnections is not supported");
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new EvalException("Writing to textConnections is not currently implemented");
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public String getClassName() {
    return "textConnection";
  }

  @Override
  public String getDescription() {
    return objectName;
  }

  @Override
  public Type getType() {
    return Type.TEXT;
  }

  @Override
  public PushbackBufferedReader getReader() throws IOException {
    return reader;
  }

  @Override
  public PrintWriter getPrintWriter() throws IOException {
    throw new EvalException("reading bytes from TextConnections is not supported");
  }

  @Override
  public void close() throws IOException {

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

}
