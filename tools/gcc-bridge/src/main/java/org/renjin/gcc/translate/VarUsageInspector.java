package org.renjin.gcc.translate;

import java.util.List;
import java.util.Map;

import org.renjin.gcc.gimple.ins.GimpleAssign;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.ins.GimpleReturn;
import org.renjin.gcc.gimple.ins.GimpleSwitch;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.expr.SymbolRef;

/**
 * Finds all variables which are addressed (&x or x[0]) within a function body.
 * 
 */
public class VarUsageInspector extends GimpleVisitor {

  private Map<Integer, VarUsage> usageMap = Maps.newHashMap();

  public VarUsageInspector(GimpleFunction fn) {
    fn.visitIns(this);
  }

  public VarUsage getUsage(int symbolId) {
    VarUsage usage = usageMap.get(symbolId);
    if (usage == null) {
      usage = new VarUsage();
      usageMap.put(symbolId, usage);
    }
    return usage;
  }

  public VarUsage getUsage(SymbolRef ref) {
    return getUsage(ref.getId());
  }

  private void visitOperand(GimpleExpr expr) {
    if (expr instanceof GimpleAddressOf) {
      visitAddressOf((GimpleAddressOf) expr);
    }
  }

  private void visitAddressOf(GimpleAddressOf expr) {
    if (expr.getValue() instanceof SymbolRef) {
      getUsage(((SymbolRef) expr.getValue()).getId()).setAddressed(true);
    }
  }

  private void visitOperands(List<GimpleExpr> expressions) {
    for (GimpleExpr expr : expressions) {
      visitOperand(expr);
    }
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {
    visitOperands(assignment.getOperands());
  }

  @Override
  public void visitCall(GimpleCall gimpleCall) {
    visitOperands(gimpleCall.getArguments());
  }

  @Override
  public void visitReturn(GimpleReturn gimpleReturn) {
    visitOperand(gimpleReturn.getValue());
  }

  @Override
  public void visitSwitch(GimpleSwitch gimpleSwitch) {
    visitOperand(gimpleSwitch.getValue());
  }

}
