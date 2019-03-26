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

import org.renjin.repackaged.guava.base.Charsets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public abstract class AbstractConnection implements Connection {

  private PushbackBufferedReader reader;
  private PrintWriter writer;
  private Charset charset;

  protected AbstractConnection(Charset charset) {
    this.charset = charset;
  }

  public AbstractConnection() {
    this(Charsets.UTF_8);
  }

  @Override
  public final PushbackBufferedReader getReader() throws IOException {
    if(this.reader == null) {
      this.reader =
          new PushbackBufferedReader(
          new InputStreamReader(getInputStream(), charset));
    }
    return this.reader;
  }

  @Override
  public final PrintWriter getPrintWriter() throws IOException {
    if(writer == null) {
      this.writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), charset));
    }
    return this.writer;
  }

  @Override
  public PrintWriter getOpenPrintWriter() {
    if(writer == null) {
      throw new IllegalStateException();
    }
    return this.writer;
  }

  @Override
  public void close() throws IOException {
    if(reader != null) {
      reader.close();
    } else {
      closeInputIfOpen();
    }
    if(writer != null) {
      writer.close();
    } else {
      closeOutputIfOpen();
    }
  }

  @Override
  public void flush() throws IOException {
    if(writer != null) {
      writer.flush();
    }
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
    return true;
  }

  protected abstract void closeInputIfOpen() throws IOException;
  protected abstract void closeOutputIfOpen() throws IOException;
}
