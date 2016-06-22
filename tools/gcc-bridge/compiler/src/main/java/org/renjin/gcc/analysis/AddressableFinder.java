package org.renjin.gcc.analysis;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.util.Collection;
import java.util.Map;

/**
 * Identifies local variables that must be addressable
 */
public class AddressableFinder {

  private GimpleSymbolTable symbolTable;
  private final Map<String, GimpleRecordTypeDef> recordTypeDefs = Maps.newHashMap();
  private Collection<GimpleCompilationUnit> units;

  public AddressableFinder(Collection<GimpleCompilationUnit> units) {
    this.units = units;
    symbolTable = new GimpleSymbolTable(units);
    for (GimpleCompilationUnit unit : units) {
      for (GimpleRecordTypeDef unitRecordType : unit.getRecordTypes()) {
        recordTypeDefs.put(unitRecordType.getId(), unitRecordType);
      }
    }
  }
  
  public void mark() {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction fn : unit.getFunctions()) {
        fn.accept(new MarkingVisitor(symbolTable.scope(fn)));
      }
    }
  }
  
  private class MarkingVisitor extends GimpleExprVisitor {
    
    private GimpleSymbolTable.Scope scope;

    public MarkingVisitor(GimpleSymbolTable.Scope scope) {
      this.scope = scope;
    }

    @Override
    public void visitAddressOf(GimpleAddressOf addressOf) {
      if(addressOf.getValue() instanceof GimpleComponentRef) {
        GimpleComponentRef ref = (GimpleComponentRef) addressOf.getValue();
        GimpleRecordType recordType = (GimpleRecordType) ref.getValue().getType();
        GimpleRecordTypeDef recordTypeDef = recordTypeDefs.get(recordType.getId());
        if(recordTypeDef == null) {
          throw new IllegalStateException("Record def not found: " + recordType);
        }

        markField(recordTypeDef, ref.getMember());
      }
      
      if(addressOf.getValue() instanceof GimpleVariableRef) {
        Optional<GimpleVarDecl> decl = scope.lookupVariable((GimpleVariableRef) addressOf.getValue());
        if (decl.isPresent()) {
          decl.get().setAddressable(true);
        } else {
          throw new IllegalStateException("Could not resolve " + addressOf.getValue());
        }
      } 
      if(addressOf.getValue() instanceof GimpleParamRef) {
        GimpleParameter param = scope.lookupParameter((GimpleParamRef) addressOf.getValue());
        param.setAddressable(true);
      }
    }
  }

  private void markField(GimpleRecordTypeDef recordTypeDef, GimpleFieldRef member) {
    GimpleField field = recordTypeDef.findField(member);

    if(field.getType() instanceof GimpleArrayType) {
      // Arrays are already addressable
      return;
    }

    field.setAddressed(true);
  }
}