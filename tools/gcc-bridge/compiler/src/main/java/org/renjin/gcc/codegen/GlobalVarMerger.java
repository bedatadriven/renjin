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
package org.renjin.gcc.codegen;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleVarDecl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * At link-time, multiple definitions of global variables are ignored,
 * except if they have an initial value defined.
 *
 */
public final class GlobalVarMerger {

  private GlobalVarMerger() {}

  public static void merge(List<GimpleCompilationUnit> units) {

    Map<String, GimpleVarDecl> canonical = new HashMap<>();

    // First add those global variables with an initial value
    // If two global variables with the same name are both initialized, then throw an error

    findDeclarationsWithInitializers(units, canonical);

    // Otherwise accept the first definition we see as the one to keep
    
    markFirstDeclarationAsCanonical(units, canonical);

    // Now mark all the remaining definitions as 'extern'

    markNonCanonical(units, canonical);
  }

  private static void markNonCanonical(List<GimpleCompilationUnit> units, Map<String, GimpleVarDecl> canonical) {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        if(varDecl.isPublic() && !varDecl.isExtern() &&
              canonical.get(varDecl.getMangledName()) != varDecl) {
          varDecl.setExtern(true);
        }
      }
    }
  }

  private static void markFirstDeclarationAsCanonical(List<GimpleCompilationUnit> units, Map<String, GimpleVarDecl> canonical) {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        if(varDecl.isPublic() && !varDecl.isExtern() &&
            !canonical.containsKey(varDecl.getMangledName())) {
          canonical.put(varDecl.getMangledName(), varDecl);
        }
      }
    }
  }

  private static void findDeclarationsWithInitializers(List<GimpleCompilationUnit> units, Map<String, GimpleVarDecl> canonical) {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        if(varDecl.isPublic() && !varDecl.isExtern() && varDecl.getValue() != null) {
          GimpleVarDecl previousValue = canonical.put(varDecl.getMangledName(), varDecl);
          if(previousValue != null) {
            throw new IllegalStateException("multiple definition of `" + varDecl.getMangledName() + "'");
          }
        }
      }
    }
  }
}
