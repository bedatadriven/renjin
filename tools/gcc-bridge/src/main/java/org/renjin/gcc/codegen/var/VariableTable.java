package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.SymbolRef;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class VariableTable {

  private final VariableTable parent;
  private Map<Integer, VarGenerator> map = new HashMap<Integer, VarGenerator>();

  public VariableTable() {
    this.parent = null;
  }

  public VariableTable(VariableTable parent) {
    this.parent = parent;
  }

  public void add(Integer gimpleId, VarGenerator variable) {
    Preconditions.checkNotNull(variable);

    map.put(gimpleId, variable);
  }

  public VarGenerator get(SymbolRef ref) {
    VarGenerator variable = map.get(ref.getId());
    if(variable == null) {
      if (parent == null) {
        throw new IllegalStateException("No variable with " + ref.getName() + " [id=" + ref.getId() + "]");
      } else {
        return parent.get(ref);
      }
    }
    return variable;
  }

  public VarGenerator get(GimpleVarDecl decl) {
    VarGenerator varGenerator = map.get(decl.getId());
    if(varGenerator == null) {
      throw new IllegalStateException("No variable named " + decl.getName() + " [id=" + decl.getId() + "]");
    }
    return varGenerator;
  }
}
