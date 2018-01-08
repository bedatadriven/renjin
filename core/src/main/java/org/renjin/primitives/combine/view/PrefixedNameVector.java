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
package org.renjin.primitives.combine.view;


import org.renjin.primitives.combine.CombinedNames;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Null;
import org.renjin.sexp.StringVector;

public class PrefixedNameVector extends StringVector {

  private String prefix;
  private AtomicVector namesVector;
  private int length;
  private boolean numberUnnamedElements;

  public PrefixedNameVector(String prefix, AtomicVector namesVector, boolean numberUnnamedElements, AttributeMap attributes) {
    super(attributes);
    this.numberUnnamedElements = numberUnnamedElements;
    this.prefix = prefix;
    this.namesVector = namesVector;
  }

  @Override
  public int length() {
    return namesVector.length();
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new PrefixedNameVector(prefix, namesVector, numberUnnamedElements, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    String name = CombinedNames.EMPTY;
    if(namesVector != Null.INSTANCE) {
      name = namesVector.getElementAsString(index);
    }
    if(CombinedNames.isPresent(name)) {
      return CombinedNames.toString(prefix) + "." + CombinedNames.toString(name);
    } else if(numberUnnamedElements) {
      return CombinedNames.toString(prefix) + Integer.toString(index+1);
    } else {
      return prefix;
    }
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
}
