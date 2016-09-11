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
