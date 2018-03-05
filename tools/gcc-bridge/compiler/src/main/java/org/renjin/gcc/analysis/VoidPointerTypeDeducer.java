/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.logging.LogManager;
import org.renjin.gcc.codegen.cpp.CppStandardLibrary;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleConditional;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.GimpleVoidType;
import org.renjin.gcc.logging.Logger;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.Set;

/**
 * Attempts to determine the types of void* pointers so that we can correctly
 * translate malloc calls.
 */
public class VoidPointerTypeDeducer implements FunctionBodyTransformer {

  public static final VoidPointerTypeDeducer INSTANCE = new VoidPointerTypeDeducer();

  public static final GimplePointerType UNKNOWN_TYPE = new GimpleVoidType().pointerTo();

  private VoidPointerTypeDeducer() {
  }

  @Override
  public boolean transform(LogManager logManager, GimpleCompilationUnit unit, GimpleFunction fn) {

    boolean updated = false;

    Logger logger = logManager.getLogger(fn, "void-deducer");

    for (GimpleVarDecl decl : fn.getVariableDeclarations()) {
      if (isVoidPtr(decl.getType())) {
        if (tryToDeduceType(logger, fn, decl)) {
          updated = true;
        }
      }
    }
    return updated;
  }

  private boolean isVoidPtr(GimpleType type) {
    return type instanceof GimplePointerType &&
        type.getBaseType() instanceof GimpleVoidType;
  }

  /**
   * Tries to deduce the type of a given void pointer declaration
   */
  private boolean tryToDeduceType(Logger logger, GimpleFunction fn, GimpleVarDecl decl) {

    Set<GimpleType> possibleTypes = Sets.newHashSet();
    fn.accept(new AssignmentFinder(decl, possibleTypes));
    fn.accept(new MemRefVisitor(decl, possibleTypes));

    logger.log("Possible type set of " + decl + " = "  + possibleTypes);

    if (possibleTypes.size() == 1 && !possibleTypes.contains(UNKNOWN_TYPE)) {
      GimpleType deducedType = possibleTypes.iterator().next();
      if (GimpleCompiler.TRACE) {
        logger.log("...resolved to " + deducedType);
      }
      decl.setType(deducedType);
      fn.accept(new VarRefTypeUpdater(decl));
      updatePointerComparisons(fn, decl);
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Updates pointer comparisons in the form {@code p == NULL} or {@code p != NULL}. In either
   * case we can infer the type of NULL from {@code p}
   */
  private void updatePointerComparisons(GimpleFunction fn, GimpleVarDecl decl) {
    
    GimpleVariableRef ref = decl.newRef();
    
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(statement instanceof GimpleConditional) {
          GimpleConditional conditional = (GimpleConditional) statement;
          if (conditional.getOperator() == GimpleOp.NE_EXPR ||
              conditional.getOperator() == GimpleOp.EQ_EXPR) {
            
            if(conditional.getOperand(0).equals(ref) && isNull(conditional.getOperand(1))) {
              conditional.getOperand(1).setType(decl.getType());

            } else if(conditional.getOperand(1).equals(ref) && isNull(conditional.getOperand(0))) {
              conditional.getOperand(0).setType(decl.getType());
            }
          }
        }
      }
    }
  }

  private boolean isNull(GimpleExpr operand) {
    return operand instanceof GimpleConstant &&
        ((GimpleConstant) operand).isNull();
    
  }

  /**
   * Looks for any cases in which the void pointer is assigned to a typed pointer
   */
  private class AssignmentFinder extends GimpleVisitor {
    private final GimpleVarDecl decl;
    private final Set<GimpleType> possibleTypes;

    public AssignmentFinder(GimpleVarDecl decl, Set<GimpleType> possibleTypes) {
      this.decl = decl;
      this.possibleTypes = possibleTypes;
    }

    @Override
    public void visitAssignment(GimpleAssignment assignment) {

      switch (assignment.getOperator()) {
        case VAR_DECL:
        case PARM_DECL:
        case NOP_EXPR:
        case ADDR_EXPR:

          GimpleExpr rhs = assignment.getOperands().get(0);
          if(isReference(rhs)) {
            inferPossibleTypes(assignment.getLHS());

          } else if(isReference(assignment.getLHS())) {
            inferPossibleTypes(rhs);
          }
          break;
        
        case POINTER_PLUS_EXPR:
          GimpleExpr pointer = assignment.getOperands().get(0);
          if(isReference(pointer)) {
            inferPossibleTypes(assignment.getLHS());
          }
          break;
      }
    }

    @Override
    public void visitCall(GimpleCall gimpleCall) {

      String functionName = findFunctionName(gimpleCall);

      if(!isMalloc(functionName) && isReference(gimpleCall.getLhs())) {

        // Assignment to our void pointer from a function call
        // e.g. pp = INTEGER(x)

        // At this point, we don't have the type of the result call, so we have
        // to give up on type inference
        possibleTypes.add(UNKNOWN_TYPE);

      } else {
        switch (functionName) {
          case "memcpy":
          case "__builtin_memcpy":
            inferFromMemCopy(gimpleCall);
            break;
          
          case "memcmp":
          case "__builtin_memcmp":
            inferFromMemCmp(gimpleCall);
            break;
        }
      }
    }

    private boolean isMalloc(String functionName) {
      switch (functionName) {
        case "malloc":
        case "calloc":
        case "alloca":
        case "realloc":
        case "__builtin_malloc__":
        case CppStandardLibrary.NEW_OPERATOR:
        case CppStandardLibrary.NEW_ARRAY_OPERATOR:
          return true;
      }
      return false;
    }

    private String findFunctionName(GimpleCall call) {
      if(call.getFunction() instanceof GimpleAddressOf) {
        GimpleAddressOf functionAddress = (GimpleAddressOf) call.getFunction();
        if(functionAddress.getValue() instanceof GimpleFunctionRef) {
          GimpleFunctionRef function = (GimpleFunctionRef) functionAddress.getValue();
          return function.getName();
        }
      }
      return "";
    }

    private void inferFromMemCmp(GimpleCall gimpleCall) {
      GimpleExpr x = gimpleCall.getOperand(0);
      GimpleExpr y = gimpleCall.getOperand(1);
      
      if(isReference(x)) {
        inferPossibleTypes(y);
      } else if(isReference(y)) {
        inferPossibleTypes(x);
      }
    }

    private void inferFromMemCopy(GimpleCall gimpleCall) {
      GimpleExpr destination = gimpleCall.getOperand(0);
      GimpleExpr source = gimpleCall.getOperand(1);
      
      if(isReference(destination)) {
        inferPossibleTypes(source);
      } else if(isReference(source)) {
        inferPossibleTypes(destination);
      }
      
      if(isReference(destination) || isReference(source)) {
        if(gimpleCall.getLhs() != null) {
          inferPossibleTypes(gimpleCall.getLhs());
        }
      }
    }

    private void inferPossibleTypes(GimpleExpr expr) {
      if(expr.getType() != null && !isVoidPtr(expr.getType())) {
        possibleTypes.add(expr.getType());
      }
    }

    /**
     * @return true if the given {@code expr} references our void pointer variable
     */
    private boolean isReference(GimpleExpr expr) {
      return expr instanceof GimpleVariableRef &&
          ((GimpleVariableRef) expr).getId() == decl.getId();
    }
  }

  /**
   * Types of void pointers can also be deduced when they are dereferenced
   * with a type.
   */
  private class MemRefVisitor extends GimpleExprVisitor {
    
    private GimpleVarDecl decl;
    private final Set<GimpleType> possibleTypes;

    public MemRefVisitor(GimpleVarDecl decl, Set<GimpleType> possibleTypes) {
      this.decl = decl;
      this.possibleTypes = possibleTypes;
    }

    @Override
    public void visitMemRef(GimpleMemRef memRef) {
      if( memRef.getPointer() instanceof GimpleVariableRef ) {
        GimpleVariableRef ref = (GimpleVariableRef) memRef.getPointer();
        if(ref.getId() == decl.getId()) {
          
          GimpleType valueType = memRef.getType();
          possibleTypes.add(new GimplePointerType(valueType));
        }
      }
    }
  }

  /**
   * Updates the type of all variable references for variables whose type has been narrowed.
   */
  private class VarRefTypeUpdater extends GimpleExprVisitor {
    
    private GimpleVarDecl decl;

    public VarRefTypeUpdater(GimpleVarDecl decl) {
      this.decl = decl;
    }

    @Override
    public void visitVariableRef(GimpleVariableRef variableRef) {
      if(variableRef.getId() == decl.getId()) {
        variableRef.setType(decl.getType());
      }
    }
  }
}
