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
package org.renjin.gcc.symbols;

import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.fun.FunctionRefGenerator;
import org.renjin.gcc.gimple.GimpleAlias;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides mapping of symbols and functions visible within the compilation unit.
 * 
 * <p>This is includes functions defined within the compilation unit.</p>
 */
public class UnitSymbolTable implements SymbolTable {
  
  private final GlobalSymbolTable globalSymbolTable;
  private final Map<Long, GExpr> variableMap = Maps.newHashMap();
  private final List<FunctionGenerator> functions = Lists.newArrayList();
  private final Map<String, FunctionGenerator> functionNameMap = Maps.newHashMap();

  /**
   * Maps variable id numbers of global variables to their mangled names.
   */
  private final Map<Long, String> mangledNameMap = new HashMap<>();

  public UnitSymbolTable(GlobalSymbolTable globalSymbolTable, GimpleCompilationUnit unit) {
    this.globalSymbolTable = globalSymbolTable;

    for (GimpleVarDecl globalDecl : unit.getGlobalVariables()) {
      mangledNameMap.put(globalDecl.getId(), globalDecl.getMangledName());
    }
  }
  
  public GExpr getVariable(GimpleSymbolRef ref) {
    GExpr expr = variableMap.get(ref.getId());
    if(expr != null) {
      return expr;
    }

    // References to global variables do not always carry the mangled name
    // For example, 'cerr' appears as just 'cerr' in the variable reference,
    // but *does* have the fully manged name '_ZSt4cerr' in the unit's
    // var declarations.

    GimpleVariableRef fixedUp = new GimpleVariableRef();
    fixedUp.setName(ref.getName());
    fixedUp.setMangledName(mangledNameMap.get(ref.getId()));
    fixedUp.setType(ref.getType());

    return globalSymbolTable.getVariable(fixedUp);
  }

  public GExpr getGlobalVariable(GimpleVarDecl decl) {
    GExpr expr = variableMap.get(decl.getId());
    if(expr == null) {
      throw new IllegalArgumentException("decl: " + decl);
    }
    return expr;
  }

  public void addGlobalVariable(GimpleVarDecl decl, GExpr globalVar) {
    variableMap.put(decl.getId(), globalVar);
    if(decl.isPublic()) {
      globalSymbolTable.addVariable(decl.getMangledName(), globalVar);
    }
  }

  public void addFunction(GimpleFunction function, FunctionGenerator functionGenerator) {

    functions.add(functionGenerator);

    for (String name : functionGenerator.getMangledNames()) {
      functionNameMap.put(name, functionGenerator);
      if(function.isPublic()) {
        globalSymbolTable.addFunction(name, new FunctionCallGenerator(functionGenerator));
      }
    }
  }

  public void addAlias(GimpleAlias alias) {
    FunctionGenerator generator = functionNameMap.get(alias.getDefinition());

    // The definition may have been pruned...
    if(generator != null) {
      generator.addAlias(alias.getAlias());
      if (alias.isPublic()) {
        globalSymbolTable.addFunction(alias.getAlias(), new FunctionCallGenerator(generator));
      }
    }
  }
  
  public Collection<FunctionGenerator> getFunctions() {
    return functions;
  }
  
  public JExpr findHandle(GimpleFunctionRef functionRef) {
    if(functionNameMap.containsKey(functionRef.getName())) {
      return new FunctionRefGenerator(functionNameMap.get(functionRef.getName()).getMethodHandle());
    }
    return globalSymbolTable.findHandle(functionRef);
  }

  public CallGenerator findCallGenerator(GimpleFunctionRef ref) {
    if(functionNameMap.containsKey(ref.getName())) {
      return new FunctionCallGenerator(functionNameMap.get(ref.getName()));
    }
    
    return globalSymbolTable.findCallGenerator(ref);
  }

}