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
package org.renjin.gnur;

import org.renjin.gcc.analysis.FlowFunction;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.ImmutableMultimap;
import org.renjin.repackaged.guava.collect.Multimap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Follows the flow of SEXP allocations through local variables
 */
public class SexpFlowFunction implements FlowFunction<Multimap<Long, GimpleCall>> {

  private Set<Long> sexpLocalVariables = new HashSet<>();

  public SexpFlowFunction(GimpleCompilationUnit unit, GimpleFunction fn) {

    // Find all the record types that map to an R SEXPREC.
    Set<String> sexpTypeIds = new HashSet<>();
    for (GimpleRecordTypeDef recordTypeDef : unit.getRecordTypes()) {
      if ("SEXPREC".equals(recordTypeDef.getName())) {
        sexpTypeIds.add(recordTypeDef.getId());
      }
    }

    // Find all the local variables that point to an SEXPREC
    for (GimpleVarDecl varDecl : fn.getVariableDeclarations()) {
      if (varDecl.getType() instanceof GimplePointerType &&
          varDecl.getType().getBaseType() instanceof GimpleRecordType) {

        GimpleRecordType recordType = varDecl.getType().getBaseType();
        if(sexpTypeIds.contains(recordType.getId())) {
          sexpLocalVariables.add(varDecl.getId());
        }
      }
    }
  }


  @Override
  public Multimap<Long, GimpleCall> initialState() {
    return ImmutableMultimap.of();
  }

  @Override
  public Multimap<Long, GimpleCall> transfer(Multimap<Long, GimpleCall> entryState, Iterable<GimpleStatement> basicBlock) {
    Multimap<Long, GimpleCall> valueMap = HashMultimap.create();
    for (GimpleStatement statement : basicBlock) {
      updateValueMap(statement, valueMap);
    }
    return valueMap;
  }

  private void updateValueMap(GimpleStatement statement, Multimap<Long, GimpleCall> valueMap) {
    // SEXPs can only be allocated by calls into Renjin, so check to see if
    // this is a call like
    //   s  = allocList
    if(statement instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) statement;
      if(call.getLhs() instanceof GimpleVariableRef) {
        GimpleVariableRef lhs = (GimpleVariableRef) call.getLhs();
        if(sexpLocalVariables.contains(lhs.getId())) {
          valueMap.put(lhs.getId(), call);
        }
      }
    }

    if(statement instanceof GimpleAssignment) {
      GimpleAssignment assignment = (GimpleAssignment) statement;
      if(assignment.getLHS() instanceof GimpleVariableRef) {
        GimpleVariableRef lhs = (GimpleVariableRef) assignment.getLHS();
        if(sexpLocalVariables.contains(lhs.getId())) {

          if(assignment.getOperator() == GimpleOp.VAR_DECL) {
            GimpleVariableRef rhs = (GimpleVariableRef) assignment.getOperands().get(0);
            if(valueMap.containsKey(rhs.getId())) {
              valueMap.putAll(lhs.getId(), valueMap.get(rhs.getId()));
            }

          } else {

            valueMap.removeAll(lhs.getId());
          }
        }
      }
    }
  }

  @Override
  public Multimap<Long, GimpleCall> join(List<Multimap<Long, GimpleCall>> inputs) {
    Multimap<Long, GimpleCall> valueMap = HashMultimap.create();
    for (Multimap<Long, GimpleCall> input : inputs) {
      valueMap.putAll(input);
    }
    return valueMap;
  }
}
