package org.renjin.gcc.analysis;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;

import java.util.HashSet;
import java.util.List;
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
      return decl.getMangledNames().get(0) + " [" + decl.getClass().getSimpleName() + "]";
    }
  }

  public static void prune(TreeLogger parentLogger, List<GimpleCompilationUnit> units) {
    
    TreeLogger logger = parentLogger.branch("Pruning Symbols");
    
    final GimpleSymbolTable symbolTable = new GimpleSymbolTable(units);

    ReferenceFinder visitor = new ReferenceFinder(symbolTable);

    // Start by adding external functions, excluding weak symbols
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        if(isEntryPoint(function)) {
          logger.debug("Retaining entry point: " + function.getName() + " [" + function.getMangledName() + "]");
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
        visitor.logger = logger.branch(TreeLogger.Level.DEBUG, "Adding references from " + retainedSymbol);
        retainedSymbol.decl.accept(visitor);
      }
    } while(visitor.changing);

    
    // Remove all but the referenced functions
    Set<GimpleDecl> retained = visitor.retainedDeclarations();
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        if(!retained.contains(function)) {
          logger.debug("Pruning function " + function.getMangledName());
        }
      }
      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        if(!retained.contains(varDecl)) {
          logger.debug("Pruning global variable " + varDecl.getName());
        }
      }
      unit.getFunctions().retainAll(retained);
      unit.getGlobalVariables().retainAll(retained);
    }

  }

  private static boolean isEntryPoint(GimpleFunction function) {
    return function.getName().equals("RS_wavelets_shrink");
//    
//    if(!function.isExtern() || function.isWeak() || function.isInline()) {
//      return false;
//    }
//    // This is a bit of hack, but assume that C++ mangled names are NOT entry
//    // points
//    if(function.getName().startsWith("_Z")) {
//      return false;
//    }
//    return true;
  }

  private static class ReferenceFinder extends GimpleExprVisitor {
    private TreeLogger logger;
    private final GimpleSymbolTable symbolTable;
    private final Set<Symbol> retained = new HashSet<>();
    private GimpleSymbolTable.Scope currentScope;
    private boolean changing = false;

    private ReferenceFinder(GimpleSymbolTable symbolTable) {
      this.symbolTable = symbolTable;
    }

    @Override
    public void visitFunctionRef(GimpleFunctionRef functionRef) {
      Optional<GimpleFunction> referencedFunction = currentScope.lookupFunction(functionRef);
      
      if(!referencedFunction.isPresent()) {
        logger.info("Reference to undefined function " + functionRef.getName());
      }
      
      if(referencedFunction.isPresent()) {
        Symbol symbol = new Symbol(referencedFunction.get(), symbolTable.scope(referencedFunction.get()));
        if(retained.add(symbol)) {
          logger.info("Adding referenced function " + symbol);
          changing = true;
        }
      }
    }
    @Override
    public void visitVariableRef(GimpleVariableRef variableRef) {
      Optional<GimpleVarDecl> decl = currentScope.lookupVariable(variableRef);
      
      if(!decl.isPresent() && !Strings.isNullOrEmpty(variableRef.getName())) {
        logger.info("Reference to undefined global variable " + variableRef.getName());
      }
      
      if(decl.isPresent()) {
        Symbol symbol = new Symbol(decl.get(), symbolTable.scope(decl.get().getUnit()));
        if(retained.add(symbol)) {
          logger.info("Adding referenced variable " + symbol);
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
