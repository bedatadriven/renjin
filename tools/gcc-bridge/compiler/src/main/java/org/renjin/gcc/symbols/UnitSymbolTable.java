package org.renjin.gcc.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Handle;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.gimple.type.GimpleType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Provides mapping of symbols and functions visible within the compilation unit.
 * 
 * <p>This is includes functions defined within the compilation unit.</p>
 */
public class UnitSymbolTable implements SymbolTable {
  
  private final GlobalSymbolTable globalSymbolTable;
  private String className;
  private final Map<Integer, Expr> variableMap = Maps.newHashMap();
  private final Map<String, List<FunctionGenerator>> functions = Maps.newHashMap();

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
    String fName = function.getName();
    if(!functions.containsKey(fName)) {
      functions.put(fName, new ArrayList<FunctionGenerator>());
    }
    functions.get(fName).add(generator);
    if(function.isExtern()) {
      globalSymbolTable.addFunction(className, generator);
    }
  }
  
  public Collection<FunctionGenerator> getFunctions() {
    List<FunctionGenerator> list = Lists.newArrayList();
    for(List<FunctionGenerator> fs : functions.values()) {
      list.addAll(fs);
    }
    return list;
  }
  
  public Handle findHandle(GimpleFunctionRef functionRef, CallingConvention callingConvention) {
    if(functions.containsKey(functionRef.getName())) {
      return functions.get(functionRef.getName()).get(0).getMethodHandle();
    }
    return globalSymbolTable.findHandle(functionRef, callingConvention);
  }

  public CallGenerator findCallGenerator(GimpleFunctionRef ref, List<GimpleExpr> operands, CallingConvention callingConvention) {
    if(functions.containsKey(ref.getName())) {
      for(FunctionGenerator functionGenerator : functions.get(ref.getName())) {
        if(functionGenerator.getFunction().getParameters().size() == operands.size()) {
          boolean matches = true;
          Iterator<GimpleParameter> it1 = functionGenerator.getFunction().getParameters().iterator();
          Iterator<GimpleExpr> it2 = operands.iterator();
          for(;matches && it1.hasNext() && it2.hasNext();) {
            GimpleType param = it1.next().getType();
            GimpleType operand = it2.next().getType();
            matches = matches && param.equals(operand);
          }
          if(matches) {
            return new FunctionCallGenerator(functionGenerator);
          }
        }
      }
    }
    return globalSymbolTable.findCallGenerator(ref, operands, callingConvention);
  }
}