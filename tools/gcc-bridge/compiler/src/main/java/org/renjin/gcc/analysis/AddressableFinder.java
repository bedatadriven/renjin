package org.renjin.gcc.analysis;

import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleComponentRef;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimpleField;
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

    boolean updated = false;
    
    Marker marker = new Marker(unit, fn);
    
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleStatement gimpleIns : basicBlock.getStatements()) {
        if(gimpleIns instanceof GimpleCall) {
          if(marker.mark(gimpleIns.getOperands())) {
            updated = true;
          }
        } else if(gimpleIns instanceof GimpleAssignment) {
          if (marker.mark(gimpleIns.getOperands())) {
            updated = true;
          }
        }
      }
    }

    return updated;
  }
  
  private class Marker {

    private final Map<Integer, GimpleVarDecl> variables = Maps.newHashMap();
    private final Map<Integer, GimpleParameter> parameters = Maps.newHashMap();
    private final Map<String, GimpleRecordTypeDef> recordTypeDefs = Maps.newHashMap();

    public Marker(GimpleCompilationUnit unit, GimpleFunction fn) {

      for (GimpleParameter param : fn.getParameters()) {
        parameters.put(param.getId(), param);
      }
      
      for (GimpleVarDecl varDecl : fn.getVariableDeclarations()) {
        variables.put(varDecl.getId(), varDecl);
      }

      for (GimpleVarDecl unitGlobalVar : unit.getGlobalVariables()){
        variables.put(unitGlobalVar.getId(), unitGlobalVar);
      }

      for (GimpleRecordTypeDef unitRecordType : unit.getRecordTypes()){
        recordTypeDefs.put(unitRecordType.getId(), unitRecordType);
      }
    }
    
    public boolean mark(List<GimpleExpr> arguments) {

      boolean updated = false;
      for (GimpleExpr expr : arguments) {
        if(expr instanceof GimpleAddressOf) {
          GimpleAddressOf addressOf = (GimpleAddressOf) expr;

          if(addressOf.getValue() instanceof GimpleComponentRef){

            GimpleComponentRef ref = (GimpleComponentRef) addressOf.getValue();
            GimpleRecordType recordType = (GimpleRecordType) ref.getValue().getType();
            GimpleRecordTypeDef recordTypeDef = recordTypeDefs.get(recordType.getId());
            if(recordTypeDef == null) {
              throw new IllegalStateException("Record def not found: " + recordType);
            }

            GimpleField field = recordTypeDef.getField(ref.memberName());
            if(!field.isAddressed()) {
              field.setAddressed(true);
              updated = true;
            }
          }

          if(addressOf.getValue() instanceof GimpleSymbolRef) {
            GimpleSymbolRef ref = (GimpleSymbolRef) addressOf.getValue();
            if(variables.containsKey(ref.getId())) {
              GimpleVarDecl decl = variables.get(ref.getId());
              if (!decl.isAddressable()) {
                decl.setAddressable(true);
                updated = true;
              }
            }
            if(parameters.containsKey(ref.getId())) {
              GimpleParameter param = parameters.get(ref.getId());
              if(!param.isAddressable()) {
                param.setAddressable(true);
                updated = true;
              }
            }
          }
        }
      }
      return updated;
    }
  }
}