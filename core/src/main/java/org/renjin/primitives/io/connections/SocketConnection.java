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
package org.renjin.primitives.io.connections;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class SocketConnection implements Connection {

  private final Socket socket;
  private PushbackBufferedReader reader;
  private PrintWriter writer;
  private String description;
  private OpenSpec openSpec = new OpenSpec("rw");
  
  public SocketConnection(String host, int port) throws UnknownHostException, IOException {
    this.socket = new Socket(host, port);
    this.description = host + ":" + port;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return socket.getOutputStream();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return socket.getInputStream();
  }

  @Override
  public PushbackBufferedReader getReader() throws IOException {
    if(this.reader == null) {
      this.reader = new PushbackBufferedReader(
          new InputStreamReader(getInputStream()));
    }
    return this.reader;
  }

  @Override
  public PrintWriter getPrintWriter() throws IOException {
    if(this.writer == null) {
      this.writer = new PrintWriter(getOutputStream());
    }
    return this.writer;
  }

  @Override
  public PrintWriter getOpenPrintWriter() {
    if(this.writer == null) {
      throw new IllegalStateException("not open");
    }
    return this.writer;
  }

  @Override
  public void close() throws IOException {
    if(this.writer != null) {
      this.writer.flush();
    }
    socket.close();
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public void flush() throws IOException {
    if(writer != null) {
      writer.flush();
    }
  }

  @Override
  public void open(OpenSpec spec) throws IOException {
    this.openSpec = spec;
  }

  @Override
  public String getClassName() {
    return "socket";
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getMode() {
    return "rw";
  }

  @Override
  public boolean canRead() {
    return true;
  }

  @Override
  public boolean canWrite() {
    return true;
  }

  @Override
  public Type getType() {
    return openSpec.getType();
  }

}
