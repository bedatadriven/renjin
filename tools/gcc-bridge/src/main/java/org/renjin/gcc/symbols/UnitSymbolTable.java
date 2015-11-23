package org.renjin.gcc.symbols;

import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.field.FieldGenerator;
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
  private final Map<Integer, FieldGenerator> variableMap = Maps.newHashMap();
  private final Map<String, FunctionGenerator> functions = Maps.newHashMap();

  public UnitSymbolTable(GlobalSymbolTable globalSymbolTable, String className) {
    this.globalSymbolTable = globalSymbolTable;
    this.className = className;
  }
  
  public ExprGenerator getVariable(GimpleSymbolRef ref) {
    FieldGenerator fieldGenerator = variableMap.get(ref.getId());
    if(fieldGenerator != null) {
      return fieldGenerator.staticExprGenerator();
    }
    
    return globalSymbolTable.getVariable(ref);
  }

  public FieldGenerator getVariable(GimpleVarDecl decl) {
    FieldGenerator fieldGenerator = variableMap.get(decl.getId());
    if(fieldGenerator == null) {
      throw new InternalCompilerException("No such global variable " + decl);
    }
    return fieldGenerator;
  }

  public void addGlobalVariable(GimpleVarDecl decl, FieldGenerator field) {
    variableMap.put(decl.getId(), field);
    if(decl.isExtern()) {
      globalSymbolTable.addVariable(decl.getName(), field.staticExprGenerator());
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
  
  public Collection<FieldGenerator> getVariables() {
    return variableMap.values();
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
