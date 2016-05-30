package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Collection;
import java.util.Map;

/**
 * Maintains a symbol lookup for purely symbolic purposes.
 */
public class GimpleSymbolTable {
  
  private class UnitTable {
    private final Map<String, GimpleFunction> functionMap = Maps.newHashMap();
    private final Map<Integer, GimpleVarDecl> globalVariables = Maps.newHashMap();

  }
  
  private class LocalTable {
    private final Map<Integer, GimpleVarDecl> localVariables = Maps.newHashMap();
  }
  
  public static interface Scope {
    
    Optional<GimpleFunction> lookupFunction(GimpleFunctionRef ref);
    
    Optional<GimpleVarDecl> lookupVariable(GimpleVariableRef ref);
    
  }
  
  private Map<GimpleCompilationUnit, UnitTable> unitMap = Maps.newHashMap();
  private Map<GimpleFunction, LocalTable> localMap = Maps.newHashMap();
  
  private Map<String, GimpleFunction> globalFunctions = Maps.newHashMap();
  private Map<String, GimpleVarDecl> globalVariables = Maps.newHashMap();
  
  public GimpleSymbolTable(Collection<GimpleCompilationUnit> units) {

    for (GimpleCompilationUnit unit : units) {
      UnitTable unitTable = new UnitTable();

      for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        unitTable.globalVariables.put(varDecl.getId(), varDecl);
        if(varDecl.isExtern()) {
          globalVariables.put(varDecl.getName(), varDecl);
        }
      }
      
      for (GimpleFunction function : unit.getFunctions()) {
        // Add the function decl to the unit- and global-scoped tables
        for (String mangledName : function.getMangledNames()) {
          unitTable.functionMap.put(mangledName, function);
          if(function.isExtern()) {
            globalFunctions.put(mangledName, function);
          }
        }
        
        // Build the local variable table for the function
        LocalTable localTable = new LocalTable();
        for (GimpleVarDecl varDecl : function.getVariableDeclarations()) {
          localTable.localVariables.put(varDecl.getId(), varDecl);
        }
        localMap.put(function, localTable);
      }
      unitMap.put(unit, unitTable);
    }
  }
  
  public Scope scope(final GimpleFunction function) {
    return new Scope() {
      @Override
      public Optional<GimpleFunction> lookupFunction(GimpleFunctionRef ref) {
        return GimpleSymbolTable.this.lookupFunction(function.getUnit(), ref.getName());
      }

      @Override
      public Optional<GimpleVarDecl> lookupVariable(GimpleVariableRef ref) {
        return GimpleSymbolTable.this.lookupGlobalVariable(function, ref);
      }
    };
  }
  
  public Scope scope(final GimpleCompilationUnit unit) {
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

        return Optional.absent();      
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

    return Optional.absent();
  }
  
  public Optional<GimpleVarDecl> lookupGlobalVariable(GimpleFunction function, GimpleVariableRef ref) {
    UnitTable unitTable = unitMap.get(function.getUnit());
    if(unitTable == null) {
      return Optional.absent();
    }
    
    LocalTable localTable = localMap.get(function);
    if(localTable == null) {
      return Optional.absent();
    }
    
    // If this a local variable reference, ignore
    if(localTable.localVariables.containsKey(ref.getId())) {
      return Optional.absent();
    }
    
    if(unitTable.globalVariables.containsKey(ref.getId())) {
      return Optional.of(unitTable.globalVariables.get(ref.getId()));
    }
    
    if(globalVariables.containsKey(ref.getName())) {
      return Optional.of(globalVariables.get(ref.getName()));
    }
    
    return Optional.absent();
  }
}
