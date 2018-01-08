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

import org.renjin.primitives.io.connections.Connection.Type;
import org.renjin.repackaged.guava.base.Strings;

/**
 * 
 * Helper class for parsing and building R language open specifications.
 *
 * Possible values are:
 * <ul>
 * <li>‘"r"’ or ‘"rt"’ Open for reading in text mode.
 * 
 * <li>‘"w"’ or ‘"wt"’ Open for writing in text mode.
 * <li>‘"a"’ or ‘"at"’ Open for appending in text mode.
 * <li>‘"rb"’ Open for reading in binary mode.
 * <li>‘"wb"’ Open for writing in binary mode.
 * <li>‘"ab"’ Open for appending in binary mode.
 * <li>‘"r+"’, ‘"r+b"’ Open for reading and writing.
 * <li>‘"w+"’, ‘"w+b"’ Open for reading and writing, truncating file
 *         initially.
 * <li>‘"a+"’, ‘"a+b"’ Open for reading and appending.
 * </ul>
 *
 */
public class OpenSpec {
  private String spec;

  public OpenSpec(String spec) {
    super();
    this.spec = Strings.nullToEmpty(spec).toLowerCase();
  }

  public boolean forReading() {
    return spec.startsWith("r") || spec.equals("w+b");
  }
  
  public boolean forWriting() {
    return spec.equals("r+") || spec.contains("w") || spec.contains("a");
  }
  
  public boolean isAppend() {
    return spec.contains("a");
  }
  
  public boolean isText() {
    return !spec.contains("b");
  }
  
  public boolean isBinary() {
    return spec.contains("b");
  }
  
  public Type getType() {
    return isText() ? Type.TEXT : Type.BINARY;
  }

  @Override
  public String toString() {
    return spec;
  }
  
}
