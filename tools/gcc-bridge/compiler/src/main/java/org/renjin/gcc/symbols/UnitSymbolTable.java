package org.renjin.gcc.symbols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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

import java.util.Collection;
import java.util.Iterator;
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
  private final Multimap<String, FunctionGenerator> functions = HashMultimap.create();

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

  public void addFunction(GimpleFunction function, FunctionGenerator generator) {
    functions.put(function.getName(), generator);
    if(function.isExtern()) {
      globalSymbolTable.addFunction(generator);
    }
  }
  
  public Collection<FunctionGenerator> getFunctions() {
    return functions.values();
  }
  
  public Handle findHandle(GimpleFunctionRef functionRef, CallingConvention callingConvention) {
    if(functions.containsKey(functionRef.getName())) {
      return functions.get(functionRef.getName()).iterator().next().getMethodHandle();
    }
    return globalSymbolTable.findHandle(functionRef, callingConvention);
  }

  public CallGenerator findCallGenerator(GimpleFunctionRef ref, List<GimpleExpr> operands, CallingConvention callingConvention) {
    Collection<FunctionGenerator> matches = functions.get(ref.getName());
    if(matches.size() == 1) {
      // If there is a single function, use this.
      // In C code there are no overloads, and types are often not an exact match.
      // It's legal to omit final arguments, for example, or implicitly cast a double* to a void*
      // and GCC doesn't also insert explicit casts.
      return new FunctionCallGenerator(matches.iterator().next());

    } else if(matches.size() > 0) {
      
      // Need to check arguments for C++ method overloading
      for (FunctionGenerator match : matches) {
        if(paramsMatch(operands, match.getFunction().getParameters())) {
          return new FunctionCallGenerator(match);
        }
      }
    }
    
    // Otherwise 
    return globalSymbolTable.findCallGenerator(ref, operands, callingConvention);
  }

  private boolean paramsMatch(List<GimpleExpr> operands, List<GimpleParameter> parameters) {
    if(operands.size() != parameters.size()) {
      return false;
    }
    Iterator<GimpleParameter> parameterIt = parameters.iterator();
    Iterator<GimpleExpr> operandIt = operands.iterator();
    while(parameterIt.hasNext()) {
      GimpleType parameterType = parameterIt.next().getType();
      GimpleType operandType = operandIt.next().getType();
      if(!parameterType.equals(operandType)) {
        return false;
      }
    }
    return true;
  }
}