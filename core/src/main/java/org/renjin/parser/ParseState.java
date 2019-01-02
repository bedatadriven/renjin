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
package org.renjin.parser;

import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

/**
 * ParseState contains the shared state of the
 * Lexer and Parser.
 * <p/>
 * These are values that were just simply all global in the C implementation.
 */
public class ParseState {
  public static final int MAXNEST = 256;

  boolean eatLines = false;

  /**
   * Whether to attach srcrefs to objects as they are parsed
   */
  boolean keepSrcRefs = false;

  /**
   * The srcfile object currently being parsed
   */
  SEXP srcFile = Null.INSTANCE;

  /**
   * The SrcFile may change
   */
  int srcFileProt;

  private FunctionSourceBuffer functionSource = new FunctionSourceBuffer();

  public FunctionSourceBuffer getFunctionSource() {
    return functionSource;
  }

  public void setEatLines(boolean eatLines) {
    this.eatLines = eatLines;
  }

  public boolean getEatLines() {
    return eatLines;
  }

  public SEXP getSrcFile() { 
    return srcFile; 
  }

  public void setSrcFile(SEXP srcFile) { 
    this.srcFile = srcFile; 
  }

  public void setKeepSrcRefs(boolean keepSrcRefs) {
    this.keepSrcRefs = keepSrcRefs;
  }
}
