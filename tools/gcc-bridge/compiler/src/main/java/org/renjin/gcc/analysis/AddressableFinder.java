/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Maps;

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
      super.visitAddressOf(addressOf);

      markExpr(addressOf.getValue());
    }

    private void markExpr(GimpleExpr expr) {
      if(expr instanceof GimpleVariableRef) {
        Optional<GimpleVarDecl> decl = scope.lookupVariable((GimpleVariableRef) expr);
        if (decl.isPresent()) {
          decl.get().setAddressable(true);
        } else {
          throw new IllegalStateException("Could not resolve " + expr);
        }
      } else if(expr instanceof GimpleParamRef) {
        GimpleParameter param = scope.lookupParameter((GimpleParamRef) expr);
        param.setAddressable(true);
      
      } else if(expr instanceof GimpleComponentRef) {
        GimpleComponentRef ref = (GimpleComponentRef) expr;
        GimpleRecordType recordType = (GimpleRecordType) ref.getValue().getType();
        GimpleRecordTypeDef recordTypeDef = recordTypeDefs.get(recordType.getId());
        if(recordTypeDef == null) {
          throw new IllegalStateException("Record def not found: " + recordType);
        }
        // TODO(alex): this test is probably too simplistic.
        if(recordTypeDef.isUnion() && ref.getMember().getType() instanceof GimpleRecordType) {
          markExpr(ref.getValue());
        } else {
          markField(recordTypeDef, ref.getMember());
        }
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