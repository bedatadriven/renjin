package org.renjin.gcc.analysis;

import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleComponentRef;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.gimple.ins.GimpleAssign;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.gimple.ins.GimpleIns;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

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

    for (GimpleVarDecl unitGlobalVar : unit.getGlobalVariables()){
      variables.put(unitGlobalVar.getId(), unitGlobalVar);
    }

    Map<String, GimpleRecordTypeDef> recordTypeDefs = Maps.newHashMap();
    for (GimpleRecordTypeDef unitRecordType : unit.getRecordTypes()){
      recordTypeDefs.put(unitRecordType.getId(), unitRecordType);
    }

    boolean updated = false;
    
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleIns gimpleIns : basicBlock.getInstructions()) {
        if(gimpleIns instanceof GimpleCall) {
          if(mark(variables, ((GimpleCall) gimpleIns).getArguments(),recordTypeDefs)) {
            updated = true;
          }
        } else if(gimpleIns instanceof GimpleAssign) {
          if (mark(variables, ((GimpleAssign) gimpleIns).getOperands(),recordTypeDefs)) {
            updated = true;
          }
        }
      }
    }

    return updated;
  }

  private boolean mark(Map<Integer, GimpleVarDecl> variables, List<GimpleExpr> arguments, Map<String, GimpleRecordTypeDef> recordTypeDefs) {
    boolean updated = false;
    for (GimpleExpr expr : arguments) {
      if(expr instanceof GimpleAddressOf) {
        GimpleAddressOf addressOf = (GimpleAddressOf) expr;

        if(addressOf.getValue() instanceof GimpleComponentRef){

          GimpleComponentRef ref = (GimpleComponentRef) addressOf.getValue();
          GimpleRecordType recordType = (GimpleRecordType) ref.getValue().getType();
          GimpleRecordTypeDef recordTypeDef = recordTypeDefs.get(recordType.getId());

          if (recordTypeDef == null){
            throw new IllegalStateException("Program in reached an undefined state, recordTypeDef = null");
          }

          if(!recordTypeDef.isAddressable()) {
            recordTypeDef.setAddressable(true);
            updated = true;
          }
        }

        if(addressOf.getValue() instanceof GimpleSymbolRef) {
          GimpleSymbolRef ref = (GimpleSymbolRef) addressOf.getValue();
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