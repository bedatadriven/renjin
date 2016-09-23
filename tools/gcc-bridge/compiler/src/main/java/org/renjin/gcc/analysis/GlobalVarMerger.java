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
package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Merge global variables declared in multiple compilation units
 */
public class GlobalVarMerger {
  
  
  public static void merge(List<GimpleCompilationUnit> units) {

    Map<String, GimpleCompilationUnit> definitionSite = Maps.newHashMap();
    
    // If the variable is initialized in one GimpleCompilationUnit, then retain that
    // declaration

    for (GimpleCompilationUnit unit : units) {
      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        if(varDecl.isExtern() && varDecl.getValue() != null) {
          definitionSite.put(varDecl.getMangledName(), unit);
        }
      }
    }
    
    // Otherwise, pick one site arbitrarily
    for (GimpleCompilationUnit unit : units) {
      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        if(varDecl.isExtern()) {
          if(!definitionSite.containsKey(varDecl.getMangledName())) {
            definitionSite.put(varDecl.getMangledName(), unit);
          }
        }
      }
    }
    
    // Remove other declarations 
    for (GimpleCompilationUnit unit : units) {
      ListIterator<GimpleVarDecl> it = unit.getGlobalVariables().listIterator();
      while(it.hasNext()) {
        GimpleVarDecl global = it.next();
        if(global.isExtern() && definitionSite.get(global.getMangledName()) != unit) {
          it.remove();
        }
      }
    }
    
  }
}
