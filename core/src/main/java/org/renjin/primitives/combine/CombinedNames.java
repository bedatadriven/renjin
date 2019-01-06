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
package org.renjin.primitives.combine;

import org.renjin.primitives.combine.view.PrefixedNameVector;
import org.renjin.primitives.sequence.RepStringVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

/**
 * Handles the combining of names from multiple list levels
 */
public class CombinedNames {

  public static final String EMPTY = "";
  
  private CombinedNames() {}


  public static StringVector combine(String prefix, Vector vector) {

    boolean hasNames = vector.getAttributes().hasNames();
    StringVector names;
    if(hasNames) {
      names = (StringVector) vector.getNames();
    } else {
      names = RepStringVector.createConstantVector(EMPTY, vector.length());
    }
    
    // Simplest case; if there is no prefix, we just use the names from the vector
    if(!isPresent(prefix)) {
      return names;
    }
    
    // The rule for deciding whether or not we name unnamed elements varies
    // depending on the type of vector
    boolean numberUnnamedElements;
    if(vector instanceof ListVector) {
      numberUnnamedElements = countUnnamedElements((ListVector) vector) > 1;
    } else {
      numberUnnamedElements = vector.length() > 1;
    }
    return new PrefixedNameVector(prefix, names, numberUnnamedElements, AttributeMap.EMPTY);
  }

  /**
   * Expands {@code NA} names to the string "NA" in preparation for concatenation. 
   * @param name an element name
   * @return "NA" if {@code isNA(name)}, otherwise {@code name}
   */
  public static String toString(String name) {
    if(StringVector.isNA(name)) {
      return "NA";
    } else {
      return name;
    }
  }

  /**
   * Return true if the name is considered present for our purposes. {@code NA} is actually
   * considered a name to preserve. The empty string is ignored.
   */
  public static boolean isPresent(String name) {
    return name == null || name.length() > 0;
  }

  public static int countUnnamedElements(ListVector list) {
    if(!list.getAttributes().hasNames()) {
      return list.length();
    }
    
    int count = 0;
    StringVector names = (StringVector) list.getNames();
    for (int i = 0; i < names.length(); i++) {
      if(!isPresent(names.getElementAsString(i))) {
        count++;
      }
    }
    return count;
  }

  public static boolean hasNames(String prefix, Vector value) {
    return isPresent(prefix) || value.getAttributes().hasNames();
  }
}
