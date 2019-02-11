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
package org.renjin.gcc.logging;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.symbols.SymbolTable;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.io.Resources;

import java.io.IOException;

public class HtmlRecordRenderer {

  private final GimpleCompilationUnit unit;
  private final SymbolTable symbolTable;


  public HtmlRecordRenderer(SymbolTable symbolTable, GimpleCompilationUnit unit) {
    this.unit = unit;
    this.symbolTable = symbolTable;
  }


  public String render() throws IOException {
    return Resources.toString(Resources.getResource(HtmlFunctionRenderer.class, "records.html"), Charsets.UTF_8)
        .replace("__RECORDS__", renderRecords());
  }

  private CharSequence renderRecords() {
    return new GimpleRenderer(symbolTable, unit).renderRecords();
  }

}
