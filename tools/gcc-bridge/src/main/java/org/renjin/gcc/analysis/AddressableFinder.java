package org.renjin.gcc.analysis;

import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.SymbolRef;
import org.renjin.gcc.gimple.ins.GimpleAssign;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.gimple.ins.GimpleIns;

import java.util.List;
import java.util.Map;

/**
 * Identifies local variables that must be addressable
 */
public class AddressableFinder implements FunctionBodyTransformer {
  
  public static final AddressableFinder INSTANCE = new AddressableFinder();
  
  @Override
  public boolean transform(GimpleCompilationUnit unit, GimpleFunction fn) {

    Map<Integer, GimpleVarDecl> variables = Maps.newHashMap();
    for (GimpleVarDecl varDecl : fn.getVariableDeclarations()) {
      variables.put(varDecl.getId(), varDecl);
    }

    for (GimpleVarDecl varDecl : unit.getGlobalVariables()){
      variables.put(varDecl.getId(),varDecl);
    }

    boolean updated = false;
    
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleIns gimpleIns : basicBlock.getInstructions()) {
        if(gimpleIns instanceof GimpleCall) {
          if(mark(variables, ((GimpleCall) gimpleIns).getArguments())) {
            updated = true;
          }
        } else if(gimpleIns instanceof GimpleAssign) {
          if (mark(variables, ((GimpleAssign) gimpleIns).getOperands())) {
            updated = true;
          }
        }
      }
    }

    return updated;
  }

  private boolean mark(Map<Integer, GimpleVarDecl> variables, List<GimpleExpr> arguments) {
    boolean updated = false;
    for (GimpleExpr expr : arguments) {
      if(expr instanceof GimpleAddressOf) {
        GimpleAddressOf addressOf = (GimpleAddressOf) expr;
        if(addressOf.getValue() instanceof SymbolRef) {
          SymbolRef ref = (SymbolRef) addressOf.getValue();
          GimpleVarDecl decl = variables.get(ref.getId());
          if(decl != null && !decl.isAddressable()) {
            decl.setAddressable(true);
            updated = true;
          }
        }
      }
    }
    return updated;
  }




}