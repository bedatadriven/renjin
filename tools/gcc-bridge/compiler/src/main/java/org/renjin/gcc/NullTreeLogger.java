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
package org.renjin.gcc;

import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.symbols.SymbolTable;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.guava.io.ByteStreams;

import java.io.PrintWriter;

/**
 * TreeLogger implementation which does nothing.
 */
public class NullTreeLogger extends TreeLogger {
  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public PrintWriter debugLog(String name) {
    return new PrintWriter(ByteStreams.nullOutputStream());
  }

  @Override
  public void dump(String dir, String file, String ext, Object value) {
  }

  @Override
  public void dumpHtml(SymbolTable symbolTable, GimpleFunction gimpleFunction, MethodNode methodNode) {

  }

  @Override
  public void log(Level level, String message) {
  }

  @Override
  public TreeLogger branch(Level level, String message) {
    return this;
  }

  @Override
  public TreeLogger debug(String message, Object code) {
    return this;
  }

  @Override
  public void finish() {
  }
}
