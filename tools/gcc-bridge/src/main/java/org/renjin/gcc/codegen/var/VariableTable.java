package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.SymbolRef;

import java.util.Map;

/**
 * 
 */
public class VariableTable {

  private final VariableTable parent;
  private Map<Integer, ExprGenerator> map = Maps.newHashMap();

  public VariableTable() {
    this.parent = null;
  }

  public VariableTable(VariableTable parent) {
    this.parent = parent;
  }

  public void add(Integer gimpleId, ExprGenerator variable) {
    Preconditions.checkNotNull(variable);
    Preconditions.checkState(!map.containsKey(gimpleId), "variable already registered with id " + gimpleId);

    map.put(gimpleId, variable);
  }

  public ExprGenerator get(SymbolRef ref) {
    ExprGenerator variable = map.get(ref.getId());
    if(variable == null) {
      if (parent == null) {
        throw new IllegalStateException("No variable with " + ref.getName() + " [id=" + ref.getId() + "]");
      } else {
        return parent.get(ref);
      }
    }
    return variable;
  }

  public ExprGenerator getIfPresent(GimpleVarDecl decl) {
    return map.get(decl.getId());
  }
  
  public ExprGenerator get(GimpleVarDecl decl) {
    ExprGenerator varGenerator = map.get(decl.getId());
    if(varGenerator == null) {
      throw new IllegalStateException("No variable named " + decl.getName() + " [id=" + decl.getId() + "]");
    }
    return varGenerator;
  }
}
