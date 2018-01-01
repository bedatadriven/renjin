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

import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.*;

/**
 * Ensures that local variables are initialized before use.
 * 
 * <p>GCC happily permits the use of uninitialized variables, but the JVM's byte code verifier
 * will abort if we try to load a variable that has not been initialized.</p>
 */
public class LocalVariableInitializer implements FunctionBodyTransformer {
  
  public static final LocalVariableInitializer INSTANCE = new LocalVariableInitializer();
  
  @Override
  public boolean transform(TreeLogger logger, GimpleCompilationUnit unit, GimpleFunction fn) {
    
    Set<Long> toInitialize = findVariablesUsedWithoutInitialization(fn);

    for (GimpleVarDecl gimpleVarDecl : fn.getVariableDeclarations()) {
      if(toInitialize.contains(gimpleVarDecl.getId())) {
        gimpleVarDecl.setValue(defaultValue(gimpleVarDecl.getType()));
      }
    }

    // one pass is always enough
    return false;
  }


  /**
   * Finds the set of variable ids that are used without initialization.
   * @param fn the function to analyze
   * @return a set of variable ids that are used without initialization
   */
  public Set<Long> findVariablesUsedWithoutInitialization(GimpleFunction fn) {

    ControlFlowGraph cfg = new ControlFlowGraph(fn);
    InitFlowFunction flowFunction = new InitFlowFunction(fn);
    DataFlowAnalysis<Set<Long>> flowAnalysis = new DataFlowAnalysis<>(cfg, flowFunction);
    if(GimpleCompiler.TRACE) {
      flowAnalysis.dump();
    }

    Set<Long> toInitialize = new HashSet<>();

    for (ControlFlowGraph.Node node : cfg.getBasicBlockNodes()) {
      // The set of variables that have been *definitely* initialized
      Set<Long> initialized = new HashSet<>(flowAnalysis.getEntryState(node));

      // Now go statement-by-statement to see if there are any possible
      // uses before definition
      for (GimpleStatement statement : node.getBasicBlock().getStatements()) {

        // Does this statement use any uninitialized variables?
        for (GimpleSymbolRef symbolRef : statement.findVariableUses()) {
          if(!initialized.contains(symbolRef.getId())) {
            toInitialize.add(symbolRef.getId());
          }
        }
        // Does this statement initialize any variables?
        updateInitializedSet(statement, initialized);
      }
    }
    return toInitialize;
  }

  private GimpleExpr defaultValue(GimpleType type) {
    if(type instanceof GimpleIntegerType) {
      return new GimpleIntegerConstant((GimpleIntegerType) type, 0);
   
    } else if(type instanceof GimpleRealType) {
      return new GimpleRealConstant((GimpleRealType) type, 0);
      
    } else if(type instanceof GimpleIndirectType) {
      return GimpleIntegerConstant.nullValue((GimpleIndirectType) type);

    } else if(type instanceof GimpleBooleanType) {
      GimpleIntegerConstant defaultValue = new GimpleIntegerConstant();
      defaultValue.setValue(0);
      defaultValue.setType(type);
      return defaultValue;
       
    } else if(type instanceof GimpleComplexType) {
      GimpleComplexConstant zero = new GimpleComplexConstant();
      GimpleRealType partType = ((GimpleComplexType) type).getPartType();
      zero.setType(type);
      zero.setIm(new GimpleRealConstant(partType, 0));
      zero.setReal(new GimpleRealConstant(partType, 0));
      return zero;
      
    } else {
      throw new UnsupportedOperationException("Don't know how to create default value for " + type);
    }
  }


  /**
   * Updates the set of initialized variables with the given statement
   * @param statement the statement
   * @param initializedVariables the set of variableIds that have definitely been initialized
   */
  private static void updateInitializedSet(GimpleStatement statement, Set<Long> initializedVariables) {
    org.renjin.repackaged.guava.base.Optional<Long> variableRef = org.renjin.repackaged.guava.base.Optional.absent();
    if (statement instanceof GimpleAssignment) {
      variableRef = findVariableRef(((GimpleAssignment) statement).getLHS());
    } else if (statement instanceof GimpleCall) {
      variableRef = findVariableRef(((GimpleCall) statement).getLhs());
    }
    if (variableRef.isPresent()) {
      initializedVariables.add(variableRef.get());
    }
  }

  private static org.renjin.repackaged.guava.base.Optional<Long> findVariableRef(GimpleExpr lhs) {
    if(lhs instanceof GimpleVariableRef) {
      GimpleVariableRef ref = (GimpleVariableRef) lhs;

      // is this a local variable or global variable?
      return org.renjin.repackaged.guava.base.Optional.of(ref.getId());

    } else if(lhs instanceof GimpleMemRef) {
      return findVariableRef(((GimpleMemRef) lhs).getPointer());
    } else if(lhs instanceof GimpleAddressOf) {
      return findVariableRef(((GimpleAddressOf) lhs).getValue());
    } else if(lhs instanceof GimpleComponentRef) {
      return findVariableRef(((GimpleComponentRef) lhs).getValue());
    } else {
      return org.renjin.repackaged.guava.base.Optional.absent();
    }
  }


  /**
   * Identifies which variables are definitely initialized at the beginning of a basic block
   */
  public static class InitFlowFunction implements FlowFunction<Set<Long>> {

    private GimpleFunction function;

    public InitFlowFunction(GimpleFunction function) {
      this.function = function;
    }

    @Override
    public Set<Long> initialState() {
      // We know the initial state of all nodes includes
      // _at least_ those variables explicitly initialized

      Set<Long> initialState = new HashSet<>();
      for (GimpleVarDecl decl : function.getVariableDeclarations()) {
        if(decl.getValue() != null) {
          initialState.add(decl.getId());
        }
        // we always have to allocate arrays and records explicitly, because our
        // arrays are stored on the heap, not the stack
        if (decl.getType() instanceof GimpleArrayType ||
            decl.getType() instanceof GimpleRecordType) {

          initialState.add(decl.getId());
        }
      }
      return initialState;
    }

    @Override
    public Set<Long> join(List<Set<Long>> inputs) {

      if(inputs.isEmpty()) {
        return Collections.emptySet();
      }

      // a local variable is known to be initialized if has been
      // initialized on ALL incoming paths
      Iterator<Set<Long>> incomingIt = inputs.iterator();

      Set<Long> state = new HashSet<>(incomingIt.next());

      while(incomingIt.hasNext()) {
        state = Sets.intersection(state, incomingIt.next());
      }
      return state;
    }

    @Override
    public Set<Long> transfer(Set<Long> entryState, Iterable<GimpleStatement> basicBlock) {
      Set<Long> exitState = new HashSet<>(entryState);
      for (GimpleStatement ins : basicBlock) {
        updateInitializedSet(ins, exitState);
      }
      return exitState;
    }

  }
}
