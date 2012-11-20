package org.renjin.gcc.translate;

import java.util.List;
import java.util.Map;

import org.renjin.gcc.gimple.GimpleAssign;
import org.renjin.gcc.gimple.GimpleCall;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleReturn;
import org.renjin.gcc.gimple.GimpleSwitch;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;

import com.google.common.collect.Maps;

/**
 * Finds all variables which are addressed (&x or x[0]) within a function
 * body. 
 *
 */
public class VarUsageInspector extends GimpleVisitor {

  private Map<String, VarUsage> usageMap = Maps.newHashMap();
  
  public VarUsageInspector(GimpleFunction fn) {
    fn.visitIns(this);
  }
  
  private VarUsage getUsage(String var) {
    VarUsage usage = usageMap.get(var);
    if(usage == null) {
      usage = new VarUsage();
      usageMap.put(var, usage);
    }
    return usage;
  }
  

  public VarUsage getUsage(GimpleParameter param) {
    return getUsage(param.getName());
  }
  
  public VarUsage getUsage(GimpleVar var) {
    return getUsage(var.getName());
  }
  
  public VarUsage getUsage(GimpleVarDecl var) {
    return getUsage(var.getName());
  }
    
  private VarUsage getUsage(GimpleExpr var) {
    return getUsage((GimpleVar)var);
  }
  
  private void visitOperand(GimpleExpr expr) {
    if(expr instanceof GimpleAddressOf) {
      visitAddressOf((GimpleAddressOf) expr);
    }
  }

  private void visitAddressOf(GimpleAddressOf expr) {
    if(expr.getExpr() instanceof GimpleVar) {
      getUsage(expr.getExpr()).setAddressed(true);
    }
  }


  private void visitOperands(List<GimpleExpr> expressions) {
    for(GimpleExpr expr : expressions) {
      visitOperand(expr);
    }
  }  
  
  @Override
  public void visitAssignment(GimpleAssign assignment) {
    visitOperands(assignment.getOperands());
  }

  @Override
  public void visitCall(GimpleCall gimpleCall) {
    visitOperands(gimpleCall.getParams());
  }

  @Override
  public void visitReturn(GimpleReturn gimpleReturn) {
    visitOperand(gimpleReturn.getValue());
  }

  @Override
  public void visitSwitch(GimpleSwitch gimpleSwitch) {
    visitOperand(gimpleSwitch.getExpr());
  }


}
