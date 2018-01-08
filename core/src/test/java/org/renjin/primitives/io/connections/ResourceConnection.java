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

import org.renjin.repackaged.guava.base.Charsets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceConnection extends AbstractConnection {
  private Class clazz;
  private String name;

  public ResourceConnection(Class clazz, String name) {
    super(Charsets.UTF_8);
    this.clazz = clazz;
    this.name = name;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    InputStream in = clazz.getResourceAsStream(name);
    if(in == null) {
      throw new IOException("Cannot open resource '" + name + "'");
    }
    return in;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void closeInputIfOpen() throws IOException {
    
  }

  @Override
  protected void closeOutputIfOpen() throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public void open(OpenSpec spec) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getClassName() {
    return "file";
  }

  @Override
  public String getDescription() {
    return name;
  }

  @Override
  public Type getType() {
    return Type.TEXT;
  }
  
  
}
