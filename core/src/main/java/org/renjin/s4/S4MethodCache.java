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
package org.renjin.s4;

import org.renjin.eval.Context;

import java.util.HashMap;
import java.util.Map;

public class S4MethodCache {

  private Map<String, S4MethodTable> methodCache = null;   // <fname, <signature, RankedMethod>>

  S4MethodCache() {
  }

  private void initializeMethodCache() {
    methodCache = new HashMap<>();
  }

  public S4MethodTable getMethod(Context context, Generic generic, String fname) {
    if(methodCache != null && methodCache.containsKey(fname)) {
      return methodCache.get(fname);
    } else {
      if(methodCache == null) {
        initializeMethodCache();
      }
      S4MethodTable methodTable = new S4MethodTable(context, generic);
      this.methodCache.put(fname, methodTable);
      return methodTable;
    }
  }

}
