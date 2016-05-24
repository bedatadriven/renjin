package org.renjin.gcc.symbols;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.fun.FunctionRefGenerator;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;

import java.util.Collection;
import java.util.List;
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
  private final List<FunctionGenerator> functions = Lists.newArrayList();
  private final Map<String, FunctionGenerator> functionNameMap = Maps.newHashMap();

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

  public void addFunction(GimpleFunction function, FunctionGenerator functionGenerator) {

    functions.add(functionGenerator);

    for (String name : functionGenerator.getMangledNames()) {
      functionNameMap.put(name, functionGenerator);
      if(function.isExtern()) {
        globalSymbolTable.addFunction(name, new FunctionCallGenerator(functionGenerator));
      }
    }
  }
  
  public Collection<FunctionGenerator> getFunctions() {
    return functions;
  }
  
  public SimpleExpr findHandle(GimpleFunctionRef functionRef) {
    if(functionNameMap.containsKey(functionRef.getName())) {
      return new FunctionRefGenerator(functionNameMap.get(functionRef.getName()).getMethodHandle());
    }
    return globalSymbolTable.findHandle(functionRef);
  }

  public CallGenerator findCallGenerator(GimpleFunctionRef ref, List<GimpleExpr> operands) {
    if(functionNameMap.containsKey(ref.getName())) {
      return new FunctionCallGenerator(functionNameMap.get(ref.getName()));
    }
    
    return globalSymbolTable.findCallGenerator(ref, operands);
  }

  public boolean isFunctionDefined(String name) {
    return functionNameMap.containsKey(name) || globalSymbolTable.isFunctionDefined(name);
  }
}