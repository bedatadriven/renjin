package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.renjin.gcc.gimple.expr.SymbolRef;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class VariableTable {
  
  private Map<Integer, VarGenerator> map = new HashMap<Integer, VarGenerator>();

  public void add(Integer gimpleId, VarGenerator variable) {
    Preconditions.checkNotNull(variable);

    map.put(gimpleId, variable);
  }

  public VarGenerator get(SymbolRef lhs) {
    VarGenerator variable = map.get(lhs.getId());
    if(variable == null) {
      throw new IllegalStateException("No variable with " + lhs.getName() + " [id=" + lhs.getId() + "]");
    }
    return variable;
  }
}
