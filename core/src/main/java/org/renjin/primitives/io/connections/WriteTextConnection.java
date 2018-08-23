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
package org.renjin.primitives.io.connections;

import org.renjin.sexp.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WriteTextConnection implements Connection {

  private Symbol objectName;
  private Environment environment;
  private boolean open = true;

  private PrintWriter printWriter;

  public WriteTextConnection(final Symbol objectName, final Environment environment) {
    this.objectName = objectName;
    this.environment = environment;
    this.printWriter = new PrintWriter(new Writer() {

      private List<String> lines = new ArrayList<>();
      private StringBuilder buffer = new StringBuilder();

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
        // add complete lines, buffer the rest
        int pos = off;
        int remaining = len;
        while(remaining > 0) {
          if(cbuf[pos] == '\n') {
            lines.add(buffer.toString());
            buffer.setLength(0);
          } else {
            buffer.append(cbuf[pos]);
          }
          pos++;
          remaining--;
        }
      }

      @Override
      public void flush() throws IOException {
        // add incomplete lines
        if(buffer.length() > 0) {
          lines.add(buffer.toString());
          buffer.setLength(0);
        }
        if(lines.size() > 0) {
          appendLines(lines);
          lines.clear();
        }
      }

      @Override
      public void close() throws IOException {
      }
    });
  }


  private void appendLines(List<String> lines) {
    SEXP output = environment.getVariableUnsafe(objectName);
    if(output == Symbol.UNBOUND_VALUE) {
      output = new StringArrayVector(lines);
    } else if(output instanceof StringVector) {
      StringVector.Builder builder = ((StringVector) output).newCopyBuilder();
      builder.addAll(lines);
      output = builder.build();
    } else {
      throw new UnsupportedOperationException();
    }

    environment.setVariableUnsafe(objectName, output);
  }

  @Override
  public void open(OpenSpec spec) throws IOException {
    environment.setVariableUnsafe(objectName, StringVector.EMPTY);
    environment.lockBinding(objectName);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PushbackBufferedReader getReader() throws IOException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PrintWriter getPrintWriter() throws IOException {
    return printWriter;
  }

  @Override
  public PrintWriter getOpenPrintWriter() {
    return printWriter;
  }

  @Override
  public void close() throws IOException {
    open = false;
    environment.unlockBinding(objectName);
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public void flush() throws IOException {
    printWriter.flush();
  }

  @Override
  public String getClassName() {
    return "textConnection";
  }

  @Override
  public String getDescription() {
    return objectName.getPrintName();
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
