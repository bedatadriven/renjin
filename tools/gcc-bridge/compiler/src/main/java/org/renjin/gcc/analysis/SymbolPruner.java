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
package org.renjin.gcc.analysis;

import org.renjin.gcc.logging.LogManager;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.logging.Logger;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Removes unused functions from the standard library
 */
public class SymbolPruner {

  private static class Symbol {
    private GimpleDecl decl;
    private GimpleSymbolTable.Scope scope;

    public Symbol(GimpleDecl decl, GimpleSymbolTable.Scope scope) {
      this.decl = decl;
      this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Symbol symbol = (Symbol) o;
      return decl.equals(symbol.decl);
    }

    @Override
    public int hashCode() {
      return decl.hashCode();
    }

    @Override
    public String toString() {
      return decl.getMangledName() + " [" + decl.getClass().getSimpleName() + "]";
    }

    public boolean isNamed() {
      return decl.getMangledName() != null;
    }
  }

  public static void prune(LogManager logManager, List<GimpleCompilationUnit> units,
                           Predicate<GimpleFunction> entryPointPredicate) {


    Logger logger = logManager.getLogger("symbol-pruner");

    final GimpleSymbolTable symbolTable = new GimpleSymbolTable(units);

    ReferenceFinder visitor = new ReferenceFinder(logger, symbolTable);

    // Start by adding external functions, excluding weak symbols
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        if(entryPointPredicate.apply(function)) {
          logger.log("Retaining entry point: " + function.getName() + " [" + function.getMangledName() + "]");
          visitor.retained.add(new Symbol(function, symbolTable.scope(function)));
        }
      }
    }
    
    // Iteratively add symbols that are then referenced by these symbols 
    // until the set stops changing
    do {
      visitor.changing = false;
      for (Symbol retainedSymbol : Lists.newArrayList(visitor.retained)) {
        visitor.currentScope = retainedSymbol.scope;
        logger.log("Adding references from " + retainedSymbol);
        retainedSymbol.decl.accept(visitor);
      }
    } while(visitor.changing);

    
    // Remove all but the referenced functions
    Set<GimpleDecl> retained = visitor.retainedDeclarations();
    for (GimpleCompilationUnit unit : units) {

      for (GimpleFunction function : unit.getFunctions()) {
        if(!retained.contains(function)) {
          logger.log("Pruning function " + function.getMangledName());
        }
      }
      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        if(!retained.contains(varDecl)) {
          logger.log("Pruning global variable " + varDecl.getName());
        }
      }

      unit.getFunctions().retainAll(retained);
      unit.getGlobalVariables().retainAll(retained);
    }
  }

  private static class ReferenceFinder extends GimpleExprVisitor {
    private Logger logger;
    private final GimpleSymbolTable symbolTable;
    private final Set<Symbol> retained = new HashSet<>();
    private GimpleSymbolTable.Scope currentScope;
    private boolean changing = false;

    private ReferenceFinder(Logger logger, GimpleSymbolTable symbolTable) {
      this.logger = logger;
      this.symbolTable = symbolTable;
    }

    @Override
    public void visitFunctionRef(GimpleFunctionRef functionRef) {
      Optional<GimpleFunction> referencedFunction = currentScope.lookupFunction(functionRef);
      
      if(!referencedFunction.isPresent()) {
        logger.log("  Reference to undefined function " + functionRef.getName());
      }

      if(referencedFunction.isPresent()) {
        Symbol symbol = new Symbol(referencedFunction.get(), symbolTable.scope(referencedFunction.get()));
        if(retained.add(symbol)) {
          if(symbol.isNamed()) {
            logger.log("  Adding referenced function " + symbol);
          }
          changing = true;
        }
      }
    }
    @Override
    public void visitVariableRef(GimpleVariableRef variableRef) {
      Optional<GimpleVarDecl> decl = currentScope.lookupVariable(variableRef);

      if(!decl.isPresent() && !Strings.isNullOrEmpty(variableRef.getName())) {
        logger.log("  Reference to undefined global variable " + variableRef.getName());
      }

      if(decl.isPresent() && decl.get().isGlobal()) {
        Symbol symbol = new Symbol(decl.get(), symbolTable.scope(decl.get().getUnit()));
        if(retained.add(symbol)) {
          logger.log("  Adding referenced variable " + symbol);
          changing = true;
        }
      }
    }
    
    public Set<GimpleDecl> retainedDeclarations() {
      Set<GimpleDecl> set = Sets.newHashSet();
      for (Symbol symbol : retained) {
        set.add(symbol.decl);
      }
      return set;
    }
  }

}
