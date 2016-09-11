package org.renjin.gcc.symbols;

import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Map;

public class LocalVariableTable implements SymbolTable {

  private final UnitSymbolTable parent;
  private Map<Integer, GExpr> variableMap = Maps.newHashMap();

  public LocalVariableTable(UnitSymbolTable parent) {
    this.parent = parent;
  }

  public void addVariable(Integer gimpleId, GExpr variable) {
    Preconditions.checkNotNull(variable);
    Preconditions.checkState(!variableMap.containsKey(gimpleId), "variable already registered with id " + gimpleId);

    variableMap.put(gimpleId, variable);
  }

  @Override
  public GExpr getVariable(GimpleSymbolRef ref) {
    GExpr variable = variableMap.get(ref.getId());
    if(variable == null) {
      if (parent == null) {
        throw new IllegalStateException("No variable with " + ref.getName() + " [id=" + ref.getId() + "]");
      } else {
        return parent.getVariable(ref);
      }
    }
    return variable;
  }

  public GExpr getVariable(GimpleVarDecl decl) {
    GExpr varGenerator = variableMap.get(decl.getId());
    if(varGenerator == null) {
      throw new IllegalStateException("No variable named " + decl.getName() + " [id=" + decl.getId() + "]");
    }
    return varGenerator;
  }

  public JExpr findHandle(GimpleFunctionRef functionRef) {
    return parent.findHandle(functionRef);
  }

  @Override
  public CallGenerator findCallGenerator(GimpleFunctionRef ref) {
    return parent.findCallGenerator(ref);
  }
}
