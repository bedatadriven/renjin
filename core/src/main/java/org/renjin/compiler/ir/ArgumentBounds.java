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
package org.renjin.compiler.ir;

import org.renjin.compiler.ir.tac.IRArgument;

/**
 * Created by parham on 8-6-17.
 */
public class ArgumentBounds {
  private String name;
  private ValueBounds valueBounds;
  
  
  public ArgumentBounds(String name, ValueBounds valueBounds) {
    this.name = name;
    this.valueBounds = valueBounds;
  }
  
  public boolean isNamed() {
    return name != null;
  }
  
  public String getName() {
    return name;
  }
  
  public ValueBounds getValueBounds() {
    return valueBounds;
  }
  
  
  public void setValueBounds(ValueBounds valueBounds) {
    this.valueBounds = valueBounds;
  }
  
  public static boolean anyNamed(Iterable<IRArgument> arguments) {
    for (IRArgument argument : arguments) {
      if(argument.isNamed()) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    if(isNamed()) {
      return name + " = " + valueBounds;
    } else {
      return valueBounds.toString();
    }
  }
}
