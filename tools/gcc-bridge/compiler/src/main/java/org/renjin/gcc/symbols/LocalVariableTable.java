package org.renjin.gcc.symbols;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;

import java.util.List;
import java.util.Map;

/**
 * 
 */
public class LocalVariableTable implements SymbolTable {

  private final UnitSymbolTable parent;
  private Map<Integer, Expr> variableMap = Maps.newHashMap();

  public LocalVariableTable(UnitSymbolTable parent) {
    this.parent = parent;
  }

  public void addVariable(Integer gimpleId, Expr variable) {
    Preconditions.checkNotNull(variable);
    Preconditions.checkState(!variableMap.containsKey(gimpleId), "variable already registered with id " + gimpleId);

    variableMap.put(gimpleId, variable);
  }
  
  public boolean isFunctionDefined(String name) {
    return parent.isFunctionDefined(name);
  }

  @Override
  public Expr getVariable(GimpleSymbolRef ref) {
    Expr variable = variableMap.get(ref.getId());
    if(variable == null) {
      if (parent == null) {
        throw new IllegalStateException("No variable with " + ref.getName() + " [id=" + ref.getId() + "]");
      } else {
        return parent.getVariable(ref);
      }
    }
    return variable;
  }

  public Expr getVariable(GimpleVarDecl decl) {
    Expr varGenerator = variableMap.get(decl.getId());
    if(varGenerator == null) {
      throw new IllegalStateException("No variable named " + decl.getName() + " [id=" + decl.getId() + "]");
    }
    return varGenerator;
  }

  public Handle findHandle(GimpleFunctionRef functionRef, CallingConvention callingConvention) {
    return parent.findHandle(functionRef, callingConvention);
  }

  @Override
  public CallGenerator findCallGenerator(GimpleFunctionRef ref, List<GimpleExpr> operands, CallingConvention callingConvention) {
    return parent.findCallGenerator(ref, operands, callingConvention);
  }
}
