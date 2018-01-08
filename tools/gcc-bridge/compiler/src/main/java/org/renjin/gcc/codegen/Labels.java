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
package org.renjin.gcc.codegen;

import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.repackaged.asm.Label;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps basic blocks to asm {@link org.renjin.repackaged.asm.Label}s
 */
public class Labels {

  private Map<Integer, Label> map = new HashMap<Integer, Label>();
  
  public Label of(GimpleBasicBlock block) {
    return of(block.getIndex());
  }
  
  public Label of(int index) {
    Label label = map.get(index);
    if(label == null) {
      label = new Label();
      map.put(index, label);
    }
    return label;
  }
  
}
