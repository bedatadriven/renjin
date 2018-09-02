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
package org.renjin.compiler.codegen;

import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.repackaged.asm.Label;

import java.util.HashMap;
import java.util.Map;

public class LabelMap {
  private Map<IRLabel, Label> map = new HashMap<>();

  public Label getBytecodeLabel(IRLabel label) {
    Label bytecodeLabel = map.get(label);
    if(bytecodeLabel == null) {
      bytecodeLabel = new Label();
      map.put(label, bytecodeLabel);
    }
    return bytecodeLabel;
  }
}
