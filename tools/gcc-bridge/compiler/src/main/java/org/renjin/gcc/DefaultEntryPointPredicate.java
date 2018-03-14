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
package org.renjin.gcc;

import org.renjin.gcc.gimple.GimpleFunction;

import java.util.function.Predicate;

/**
 * Identifies functions which are considered "entry points" and must definitely
 * be compiled. Other functions may be removed if they are not used.
 */
public class DefaultEntryPointPredicate implements Predicate<GimpleFunction> {
  @Override
  public boolean test(GimpleFunction function) {

    if(!function.isPublic() || function.isWeak() || function.isInline()) {
      return false;
    }
    // This is a bit of hack, but assume that C++ mangled names are NOT entry
    // points
    if(function.getName().startsWith("_Z")) {
      return false;
    }
    return true;
  }
}
