package org.renjin.gcc.symbols;

import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;

import java.util.Collection;
import java.util.Map;

/**
 * Provides mapping of symbols and functions visible within the compilation unit.
 * 
 * <p>This is includes functions defined within the compilation unit.</p>
 */
public class UnitSymbolTable implements SymbolTable {
  
  private final GlobalSymbolTable globalSymbolTable;
  private String className;
  private final Map<Integer, Expr> variableMap = Maps.newHashMap();
  private final Map<String, FunctionGenerator> functions = Maps.newHashMap();

  public UnitSymbolTable(GlobalSymbolTable globalSymbolTable, String className) {
    this.globalSymbolTable = globalSymbolTable;
    this.className = className;
  }
  
  public Expr getVariable(GimpleSymbolRef ref) {
    Expr expr = variableMap.get(ref.getId());
    if(expr != null) {
      return expr;
    }
    
    return globalSymbolTable.getVariable(ref);
  }

  public Expr getGlobalVariable(GimpleVarDecl decl) {
    Expr expr = variableMap.get(decl.getId());
    if(expr == null) {
      throw new IllegalArgumentException("decl: " + decl);
    }
    return expr;
  }

  public void addGlobalVariable(GimpleVarDecl decl, Expr globalVar) {
    variableMap.put(decl.getId(), globalVar);
    if(decl.isExtern()) {
      globalSymbolTable.addVariable(decl.getName(), globalVar);
    }
  }

  public void addFunction(String className, GimpleFunction function, FunctionGenerator generator) {
    functions.put(function.getName(), generator);
    if(function.isExtern()) {
      globalSymbolTable.addFunction(className, generator);
    }
  }
  
  public Collection<FunctionGenerator> getFunctions() {
    return functions.values();
  }
  
  public Handle findHandle(GimpleFunctionRef functionRef, CallingConvention callingConvention) {
    if(functions.containsKey(functionRef.getName())) {
      return functions.get(functionRef.getName()).getMethodHandle();
    }
    return globalSymbolTable.findHandle(functionRef, callingConvention);
  }

  public CallGenerator findCallGenerator(GimpleFunctionRef ref, CallingConvention callingConvention) {
    if(functions.containsKey(ref.getName())) {
      FunctionGenerator functionGenerator = functions.get(ref.getName());
      return new FunctionCallGenerator(functionGenerator);
    }
    return globalSymbolTable.findCallGenerator(ref, callingConvention);
  }


}
