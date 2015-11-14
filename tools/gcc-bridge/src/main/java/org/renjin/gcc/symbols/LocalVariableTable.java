package org.renjin.gcc.symbols;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.SymbolRef;

import java.util.Map;

/**
 * 
 */
public class LocalVariableTable implements SymbolTable {

  private final UnitSymbolTable parent;
  private Map<Integer, ExprGenerator> variableMap = Maps.newHashMap();

  public LocalVariableTable(UnitSymbolTable parent) {
    this.parent = parent;
  }

  public void addVariable(Integer gimpleId, ExprGenerator variable) {
    Preconditions.checkNotNull(variable);
    Preconditions.checkState(!variableMap.containsKey(gimpleId), "variable already registered with id " + gimpleId);

    variableMap.put(gimpleId, variable);
  }

  @Override
  public ExprGenerator getVariable(SymbolRef ref) {
    ExprGenerator variable = variableMap.get(ref.getId());
    if(variable == null) {
      if (parent == null) {
        throw new IllegalStateException("No variable with " + ref.getName() + " [id=" + ref.getId() + "]");
      } else {
        return parent.getVariable(ref);
      }
    }
    return variable;
  }

  public ExprGenerator getVariable(GimpleVarDecl decl) {
    ExprGenerator varGenerator = variableMap.get(decl.getId());
    if(varGenerator == null) {
      throw new IllegalStateException("No variable named " + decl.getName() + " [id=" + decl.getId() + "]");
    }
    return varGenerator;
  }

  public Handle findHandle(GimpleFunctionRef functionRef, CallingConvention callingConvention) {
    return parent.findHandle(functionRef, callingConvention);
  }

  public CallGenerator findCallGenerator(GimpleFunctionRef ref, CallingConvention callingConvention) {
    return parent.findCallGenerator(ref, callingConvention);
  }
}
