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
import java.net.URL;

public class UrlConnection extends AbstractConnection {

  private final URL url;
  private InputStream in;  
  private OpenSpec openSpec = new OpenSpec("r");

  public UrlConnection(URL url) {
    super();
    this.url = url;
  }

  @Override
  public void open(OpenSpec spec) throws IOException {
    this.openSpec = spec;
    if(spec.forWriting()) {
      throw new EvalException("Cannot open url connection for writing");
    } else {
      if(spec.isText()) {
        getReader();
      } else {
        getInputStream();
      }
    }
  }

  
  @Override
  public InputStream getInputStream() throws IOException {
    if(in == null) {
      this.in = url.openStream();
    }
    return this.in;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new EvalException("Cannot open URL for output");
  }

  @Override
  public boolean isOpen() {
    return in != null;
  }

  @Override
  protected void closeInputIfOpen() throws IOException {
    in.close();
  }

  @Override
  protected void closeOutputIfOpen() throws IOException {    
  }

  @Override
  public String getClassName() {
    return "url";
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
  public String getDescription() {
    return url.toExternalForm();
  }

  @Override
  public Type getType() {
    return openSpec.getType();
  }
}
