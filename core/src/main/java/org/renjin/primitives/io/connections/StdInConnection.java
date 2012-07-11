/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.io.connections;


import org.renjin.eval.EvalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;



/**
 * Connection object for the standard input stream.
 * Currently a stub, will throw an exception if actually used.
 */
public class StdInConnection implements Connection {

  private InputStream stream = System.in;

  public void setInputStream(InputStream in) {
    this.stream = in;
  }
  
  @Override
  public InputStream getInputStream() throws IOException {
    return stream; // TODO
  }

  @Override
  public PrintWriter getPrintWriter() throws IOException {
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
  public PushbackBufferedReader getReader() throws IOException {
    throw new EvalException("implement me!");
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
