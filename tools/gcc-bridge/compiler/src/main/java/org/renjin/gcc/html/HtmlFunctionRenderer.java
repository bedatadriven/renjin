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
package org.renjin.gcc.html;

import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.symbols.SymbolTable;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.io.Resources;

import java.io.IOException;

public class HtmlFunctionRenderer {
  private SymbolTable symbolTable;
  private GimpleFunction gimpleFunction;
  private MethodNode methodNode;


  public HtmlFunctionRenderer(SymbolTable symbolTable, GimpleFunction gimpleFunction, MethodNode methodNode) {
    this.symbolTable = symbolTable;
    this.gimpleFunction = gimpleFunction;
    this.methodNode = methodNode;
  }

  public String render() throws IOException {
    return Resources.toString(Resources.getResource(HtmlFunctionRenderer.class, "function.html"), Charsets.UTF_8)
        .replace("__INPUT_SOURCE__", renderInputSource())
        .replace("__GIMPLE__", renderGimple())
        .replace("__BYTECODE__", renderBytecode());
  }


  private String renderInputSource() {
    StringBuilder html = new StringBuilder();
    for (InputSource source : InputSource.from(gimpleFunction)) {
      source.render(html);
    }
    return html.toString();
  }


  private CharSequence renderGimple() {
    return new GimpleRenderer(symbolTable, gimpleFunction).render();
  }

  private CharSequence renderBytecode() {
    return new BytecodeRenderer(methodNode).render();
  }

}
