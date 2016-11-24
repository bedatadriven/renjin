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

import org.renjin.gcc.codegen.cpp.CppStandardLibrary;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.*;

/**
 * Identifies Record types that are used in ways that invalidate the unit pointer assumption.
 *
 * <p>Basically, we really want to assume that each pointer to a given record type points to EXACTLY one 
 * record value. If this assumption holds true, then we can use the 
 * {@link RecordUnitPtrStrategy} and represent all pointers of this record type 
 * as simple JVM references.</p>
 *
 * <p>If there are any violations of this assumption, then we must fallback to using the
 * {@link RecordFatPtrStrategy}</p>
 */
public class RecordUsageAnalyzer  {

  private static final Set<String> MALLOC_FUNCTIONS = Sets.newHashSet(
      "malloc",
      "alloca",
      "realloc",
      "calloc",
      "__builtin_malloc",
      CppStandardLibrary.NEW_ARRAY_OPERATOR,
      CppStandardLibrary.NEW_OPERATOR);

  private Map<String, GimpleRecordTypeDef> map = Maps.newHashMap();

  /**
   * The set of all RecordTypeDef ids for which the unit record pointer assumptions hold
   */
  @VisibleForTesting
  Set<String> unitPointerAssumptionsHold = new HashSet<>();


  public RecordUsageAnalyzer(Collection<GimpleRecordTypeDef> recordTypeDefs) {
    for (GimpleRecordTypeDef recordTypeDef : recordTypeDefs) {
      map.put(recordTypeDef.getId(), recordTypeDef);
    }
  }

  public void analyze(List<GimpleCompilationUnit> units) {
    checkUnitRecordPtrAssumptions(units);

  }

  private void checkUnitRecordPtrAssumptions(List<GimpleCompilationUnit> units) {

    // Start by assuming that all pointers to all record types point to exactly one record
    // (and not a contiguous block of memory containing several records)
    unitPointerAssumptionsHold.addAll(map.keySet());

    // we are looking for any usage that violates the unit record pointer assumption.
    // This includes:

    // (1) Variables or Fields which are arrays of this record type
    // (2) Malloc(s) of this record type where it cannot be statically verified that s = sizeof(record_t)
    // (3) Casting of second-level indirection (record**) to (void**) 

    checkForArrayDeclarations(units);
    checkForRecordMallocs(units);
    checkForCastingToVoidPP(units);
  }
  
  /**
   * Check for any declarations of arrays of records, which imply a contiguous block
   * of more than one records.
   */
  private void checkForArrayDeclarations(List<GimpleCompilationUnit> units) {
    for (GimpleRecordTypeDef recordTypeDef : map.values()) {
      for (GimpleField gimpleField : recordTypeDef.getFields()) {
        checkForRecordArray(gimpleField.getType());
      }
    }

    for (GimpleCompilationUnit unit : units) {
      for (GimpleVarDecl gimpleVarDecl : unit.getGlobalVariables()) {
        checkForRecordArray(gimpleVarDecl.getType());
      }
      for (GimpleFunction function : unit.getFunctions()) {
        for (GimpleVarDecl gimpleVarDecl : function.getVariableDeclarations()) {
          checkForRecordArray(gimpleVarDecl.getType());
        }
      }
    }
  }

  private void checkForRecordArray(GimpleType type) {
    if(type instanceof GimpleArrayType) {
      GimpleType componentType = ((GimpleArrayType) type).getComponentType();
      if(componentType instanceof GimpleRecordType) {
        GimpleRecordType recordType = (GimpleRecordType) componentType;
        unitPointerAssumptionsHold.remove(recordType.getId());
      }
    }
  }

  private void checkForRecordMallocs(List<GimpleCompilationUnit> units) {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
          for (GimpleStatement statement : basicBlock.getStatements()) {
            if(statement instanceof GimpleCall) {
              GimpleCall call = (GimpleCall) statement;
              String functionName = getFunctionName(call.getFunction());
              if(MALLOC_FUNCTIONS.contains(functionName)) {
                checkForRecordMalloc(functionName, call);
              }
            }
          }
        }
      }
    }
  }


  private String getFunctionName(GimpleExpr functionExpr) {
    if (functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return ref.getName();
      }
    }
    return null;
  }


  /**
   * Check for assignment from (record**) to (void**). 
   * 
   * <p>Once a value has been casted to {@code void**}, it can be ultimately be assigned 
   * to the result of a {@code malloc} call in some other function.</p>
   */
  private void checkForCastingToVoidPP(List<GimpleCompilationUnit> units) {

    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
          for (GimpleStatement statement : basicBlock.getStatements()) {
            if(statement instanceof GimpleCall) {
              for (GimpleExpr operand : statement.getOperands()) {
                checkForPointerPointerType(operand.getType());
              }
            }
          }
        }
      }
    }
  }

  private void checkForPointerPointerType(GimpleType type) {
    if(type instanceof GimpleIndirectType) {
      GimpleType baseType = type.getBaseType();
      if(baseType instanceof GimpleIndirectType) {
        if(baseType.getBaseType() instanceof GimpleRecordType) {
          GimpleRecordType baseBaseType = baseType.getBaseType();
          unitPointerAssumptionsHold.remove(baseBaseType.getId());
        }
      }
    }
  }


  private void checkForRecordMalloc(String functionName, GimpleCall mallocCall) {

    GimpleType pointerType = mallocCall.getLhs().getType();
    assert pointerType instanceof GimplePointerType : "Malloc must be assigned to a pointer";
    
    if(pointerType.getBaseType() instanceof GimpleRecordType) {
      GimpleRecordType recordType = pointerType.getBaseType();

      // struct record_t *p = malloc(size) does NOT violate our assumption
      // as long as size == sizeof(record_t)
      if(functionName.equals("calloc")) {
        GimpleExpr elements = mallocCall.getOperands().get(0);
        GimpleExpr elementSize = mallocCall.getOperands().get(1);

        if (!staticallyEqual(elements, 1) ||
            !staticallyEqual(elementSize, recordType.sizeOf())) {
          unitPointerAssumptionsHold.remove(recordType.getId());
        }

      } else {
        GimpleExpr size = mallocCall.getOperands().get(0);
        if (!staticallyEqual(size, recordType.sizeOf())) {
          unitPointerAssumptionsHold.remove(recordType.getId());
        }
      }
    }
  }

  private boolean staticallyEqual(GimpleExpr expr, int value) {
    if(expr instanceof GimpleIntegerConstant) {
      GimpleIntegerConstant constant = (GimpleIntegerConstant) expr;
      
      return constant.getNumberValue().intValue() == value;
    }
    return false;
  }
  
  public boolean unitPointerAssumptionsHoldFor(GimpleRecordTypeDef recordTypeDef) {
    return unitPointerAssumptionsHold.contains(recordTypeDef.getId());
  }

}

