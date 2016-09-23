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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;



public class StdOutConnection implements Connection {

  private PrintWriter stream = new PrintWriter(System.out);

  public void setOutputStream(PrintWriter out) {
    this.stream = out;
  }
  
  @Override
  public InputStream getInputStream() throws IOException {
    throw new EvalException("cannot read from stdout");
  }

  @Override
  public PushbackBufferedReader getReader() throws IOException {
    throw new EvalException("cannot read from stdout");
  }
  
  @Override
  public PrintWriter getPrintWriter()  {
    return stream;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    // todo: fix me
    throw new EvalException("Cannot open stdout for binary output, only text (todo?)");
  }

  @Override
  public boolean isOpen() {
    return true;
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
    return "stdout";
  }

  @Override
  public String getMode() {
    return "w";
  }

  @Override
  public boolean canRead() {
    return false;
  }

  @Override
  public boolean canWrite() {
    return true;
  }

  @Override
  public Type getType() {
    return Type.TEXT;
  }
}
