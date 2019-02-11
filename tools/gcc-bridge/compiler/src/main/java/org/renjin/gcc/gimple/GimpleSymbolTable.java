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
package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleParamRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Maintains a symbol lookup for purely symbolic purposes.
 */
public class GimpleSymbolTable {
  
  private class UnitTable {
    private final Map<String, GimpleFunction> functionMap = Maps.newHashMap();
    private final Map<Long, GimpleVarDecl> globalVariables = Maps.newHashMap();

  }
  
  private class LocalTable {
    private final Map<Long, GimpleVarDecl> localVariables = Maps.newHashMap();
  }
  
  public static interface Scope {
    
    Optional<GimpleFunction> lookupFunction(GimpleFunctionRef ref);
    
    Optional<GimpleVarDecl> lookupVariable(GimpleVariableRef ref);

    GimpleParameter lookupParameter(GimpleParamRef ref);

  }
  
  private Map<GimpleCompilationUnit, UnitTable> unitMap = Maps.newHashMap();
  private Map<GimpleFunction, LocalTable> localMap = Maps.newHashMap();
  
  private Map<String, GimpleFunction> globalFunctions = Maps.newHashMap();
  private Map<String, GimpleVarDecl> globalVariables = Maps.newHashMap();
  
  public GimpleSymbolTable(Collection<GimpleCompilationUnit> units) {

    for (GimpleCompilationUnit unit : units) {
      UnitTable unitTable = new UnitTable();

      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        if(!varDecl.isExtern()) {
          unitTable.globalVariables.put(varDecl.getId(), varDecl);
          if (varDecl.isPublic()) {
            globalVariables.put(varDecl.getName(), varDecl);
          }
        }
      }
      
      for (GimpleFunction function : unit.getFunctions()) {
        // Add the function decl to the unit- and global-scoped tables
        unitTable.functionMap.put(function.getMangledName(), function);
        if(function.isPublic()) {
          globalFunctions.put(function.getMangledName(), function);
        }

        // Build the local variable table for the function
        LocalTable localTable = new LocalTable();
        for (GimpleVarDecl varDecl : function.getVariableDeclarations()) {
          localTable.localVariables.put(varDecl.getId(), varDecl);
        }
        localMap.put(function, localTable);
      }

      for (GimpleAlias alias : unit.getAliases()) {

        GimpleFunction definition = unitTable.functionMap.get(alias.getDefinition());
        unitTable.functionMap.put(alias.getAlias(), definition);
        if(alias.isPublic()) {
          globalFunctions.put(alias.getAlias(), definition);
        }
      }

      unitMap.put(unit, unitTable);
    }
  }
  
  public Scope scope(final GimpleFunction function) {
    
    final Map<Long, GimpleParameter> paramMap = Maps.newHashMap();
    for (GimpleParameter param : function.getParameters()) {
      paramMap.put(param.getId(), param);
    }

    final Map<Long, GimpleVarDecl> localVarMap = Maps.newHashMap();
    for (GimpleVarDecl varDecl : function.getVariableDeclarations()) {
      localVarMap.put(varDecl.getId(), varDecl);
    }

    return new Scope() {
      @Override
      public Optional<GimpleFunction> lookupFunction(GimpleFunctionRef ref) {
        return GimpleSymbolTable.this.lookupFunction(function.getUnit(), ref.getName());
      }

      @Override
      public Optional<GimpleVarDecl> lookupVariable(GimpleVariableRef ref) {
        if(localVarMap.containsKey(ref.getId())) {
          return Optional.of(localVarMap.get(ref.getId()));
        }
        return GimpleSymbolTable.this.lookupGlobalVariable(function, ref);
      }

      @Override
      public GimpleParameter lookupParameter(GimpleParamRef ref) {
        GimpleParameter param = paramMap.get(ref.getId());
        if(param == null) {
          throw new IllegalStateException("Cannot resolve ParamRef " + ref);
        }
        return param;
      }
    };
  }
  
  public Scope scope(final GimpleCompilationUnit unit) {

    Preconditions.checkNotNull(unit, "unit");

    final UnitTable unitTable = unitMap.get(unit);

    return new Scope() {
      @Override
      public Optional<GimpleFunction> lookupFunction(GimpleFunctionRef ref) {
        return GimpleSymbolTable.this.lookupFunction(unit, ref.getName());
      }

      @Override
      public Optional<GimpleVarDecl> lookupVariable(GimpleVariableRef ref) {

        if(unitTable.globalVariables.containsKey(ref.getId())) {
          return Optional.of(unitTable.globalVariables.get(ref.getId()));
        }

        if(globalVariables.containsKey(ref.getName())) {
          return Optional.of(globalVariables.get(ref.getName()));
        }

        return Optional.empty();
      }

      @Override
      public GimpleParameter lookupParameter(GimpleParamRef ref) {
        throw new UnsupportedOperationException();
      }
    }; 
  }

  public Optional<GimpleFunction> lookupFunction(GimpleCompilationUnit unit, String name) {
    UnitTable unitTable = unitMap.get(unit);
    if(unitTable != null) {
      if (unitTable.functionMap.containsKey(name)) {
        return Optional.of(unitTable.functionMap.get(name));
      }
    }
    
    if(globalFunctions.containsKey(name)) {
      return Optional.of(globalFunctions.get(name));
    }

    return Optional.empty();
  }
  
  public Optional<GimpleVarDecl> lookupGlobalVariable(GimpleFunction function, GimpleVariableRef ref) {
    UnitTable unitTable = unitMap.get(function.getUnit());
    if(unitTable == null) {
      return Optional.empty();
    }
    
    LocalTable localTable = localMap.get(function);
    if(localTable == null) {
      return Optional.empty();
    }
    
    // If this a local variable reference, ignore
    if(localTable.localVariables.containsKey(ref.getId())) {
      return Optional.empty();
    }
    
    if(unitTable.globalVariables.containsKey(ref.getId())) {
      return Optional.of(unitTable.globalVariables.get(ref.getId()));
    }
    
    if(globalVariables.containsKey(ref.getName())) {
      return Optional.of(globalVariables.get(ref.getName()));
    }
    
    return Optional.empty();
  }
}
