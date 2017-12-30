/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.analysis;

import com.google.common.collect.Sets;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.IFDSTabulationProblem;
import heros.flowfunc.Identity;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleParamRef;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleReturn;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.repackaged.guava.base.Optional;

import java.util.*;

public class PointsToAnalysis implements IFDSTabulationProblem<
    GimpleNode,
    PointsTo,
    GimpleFunction,
    GimpleInterproceduralCFG> {

  private final GimpleSymbolTable symbolTable;
  private final GimpleInterproceduralCFG cfg;

  public PointsToAnalysis(GimpleInterproceduralCFG cfg) {
    this.symbolTable = cfg.getSymbolTable();
    this.cfg = cfg;
  }

  @Override
  public GimpleInterproceduralCFG interproceduralCFG() {
    return cfg;
  }

  @Override
  public Map<GimpleNode, Set<PointsTo>> initialSeeds() {
    Map<GimpleNode, Set<PointsTo>> seeds = new HashMap<>();
    for (GimpleFunction gimpleFunction : cfg.getEntryPoints()) {
      for (GimpleNode startPoint : cfg.getStartPointsOf(gimpleFunction)) {
        seeds.put(startPoint, Collections.singleton(zeroValue()));
      }
    }
    return seeds;
  }


  @Override
  public FlowFunctions<GimpleNode, PointsTo, GimpleFunction> flowFunctions() {
    return new FlowFunctions<GimpleNode, PointsTo, GimpleFunction>() {
      @Override
      public FlowFunction<PointsTo> getNormalFlowFunction(final GimpleNode curr, GimpleNode succ) {

        if(Malloc.isMalloc(curr.getStatement())) {
          final GimpleExpr lhs = ((GimpleCall) curr.getStatement()).getLhs();
          final Allocation allocation = new HeapAllocation(curr.getStatement());

          return new AllocFlowFunction(lhs, allocation);
        }

        if(curr.getStatement() instanceof GimpleAssignment) {
          final GimpleAssignment assignment = (GimpleAssignment) curr.getStatement();
          if (assignment.getLHS().getType() instanceof GimpleIndirectType) {

            if (assignment.getOperator() == GimpleOp.ADDR_EXPR) {
              final GimpleExpr lhs = assignment.getLHS();
              GimpleAddressOf rhs = (GimpleAddressOf) assignment.getOperands().get(0);
              final Allocation allocation = resolveAllocation(curr.getFunction(), rhs.getValue());

              return new FlowFunction<PointsTo>() {
                @Override
                public Set<PointsTo> computeTargets(PointsTo source) {
                  if (source == PointsTo.ZERO) {
                    // source: 0
                    // assignment: lhs = &x
                    // We know that lhs now points to the stack allocation y
                    return Collections.singleton(new PointsTo(lhs, allocation));

                  } else if (source.getExpr().equals(lhs)) {
                    // source: lhs :=
                    // assignment: lhs = &x
                    return Collections.emptySet();

                  } else {
                    // We have nothing to say about the source fact...
                    return Collections.singleton(source);
                  }
                }
              };

            } else if (assignment.getOperator() == GimpleOp.POINTER_PLUS_EXPR) {
              final GimpleExpr lhs = assignment.getLHS();
              final GimpleExpr pointerExpr = assignment.getOperands().get(0);
              final GimpleExpr offsetPtr = assignment.getOperands().get(1);


              return new FlowFunction<PointsTo>() {
                @Override
                public Set<PointsTo> computeTargets(PointsTo source) {

                  if (source.getExpr().equals(pointerExpr)) {
                    // source: p2 = x
                    // assignment : p1 = p2 + 8
                    // Therefore, we know p1 = x+8, AND p2 is still x
                    return Sets.newHashSet(
                        source,
                        source.withOffset(lhs, offsetPtr));

                  } else if (source.getExpr().equals(lhs)) {
                    // source: p1 = y
                    // assignment: p1 = p2 + 8
                    // Therefore, we can kill the previous fact, p1 has a new value.
                    return Collections.emptySet();

                  } else {
                    // Otherwise, no impact.
                    return Collections.singleton(source);
                  }
                }
              };
            } else if (assignment.getOperator() == GimpleOp.VAR_DECL ||
                assignment.getOperator() == GimpleOp.PARM_DECL) {

              final GimpleExpr lhs = assignment.getLHS();
              final GimpleExpr rhs = assignment.getOperands().get(0);

              return new AliasFlowFunction(lhs, rhs);
            }
          }
        }
        return Identity.v();
      }

      @Override
      public FlowFunction<PointsTo> getCallFlowFunction(final GimpleNode callStmt, GimpleFunction destinationMethod) {
        GimpleCall call = (GimpleCall) callStmt.getStatement();
        final List<GimpleExpr> callArgs = call.getOperands();
        final List<GimpleParamRef> paramLocals = new ArrayList<>();
        for (int i = 0; i < destinationMethod.getParameters().size(); i++) {
          paramLocals.add(new GimpleParamRef(destinationMethod.getParameters().get(i)));
        }
        return new FlowFunction<PointsTo>() {
          public Set<PointsTo> computeTargets(PointsTo source) {
            GimpleExpr value = source.getExpr();
            int argIndex = callArgs.indexOf(value);
            if (argIndex > -1) {
              return Collections.singleton(new PointsTo(paramLocals.get(argIndex), source.getAllocation()));
            }
            return Collections.emptySet();
          }
        };
      }

      @Override
      public FlowFunction<PointsTo> getReturnFlowFunction(GimpleNode callSite, GimpleFunction calleeMethod,
                                                          GimpleNode exitStmt, GimpleNode returnSite) {
        if (exitStmt.getStatement() instanceof GimpleReturn) {
          GimpleCall call = (GimpleCall) callSite.getStatement();
          if (call.getLhs() != null) {
            GimpleExpr lhs = call.getLhs();
            GimpleReturn returnStmt = (GimpleReturn) exitStmt.getStatement();
            GimpleExpr returnValue = returnStmt.getValue();

            if(returnValue instanceof GimpleVariableRef || returnValue instanceof GimpleParamRef) {
              return new AliasFlowFunction(lhs, returnValue);
            } else {
              throw new UnsupportedOperationException("returnValue: " + returnValue.getClass().getName());
            }
          }
        }
        return Identity.v();
      }

      @Override
      public FlowFunction<PointsTo> getCallToReturnFlowFunction(GimpleNode callSite, GimpleNode returnSite) {
        return Identity.v();
      }
    };
  }

  /**
   * Finds the symbolic allocationt that corresponds to the given Gimple expr.
   *
   * <p>This could be an allocation resulting from a local variable declaration,
   * a parameter, or global variable.</p>
   *
   * @param function
   * @param value
   * @return
   */
  private Allocation resolveAllocation(GimpleFunction function, GimpleExpr value) {
    GimpleSymbolTable.Scope scope = symbolTable.scope(function);
    if(value instanceof GimpleVariableRef) {
      Optional<GimpleVarDecl> decl = scope.lookupVariable(((GimpleVariableRef) value));
      if (!decl.isPresent()) {
        throw new IllegalStateException("Cannot resolve variable ref " + value + " in " + function.getMangledName());
      }
      if(decl.get().isGlobal()) {
        return new GlobalVarAllocation(decl.get());
      } else {
        return new StackAllocation(function, decl.get());
      }
    } else if(value instanceof GimpleParamRef) {
      GimpleParameter parameter = scope.lookupParameter(((GimpleParamRef) value));
      throw new UnsupportedOperationException("TODO");

    } else {
      throw new UnsupportedOperationException("TODO: " + value.getClass().getSimpleName());
    }
  }

  @Override
  public PointsTo zeroValue() {
    return PointsTo.ZERO;
  }

  @Override
  public boolean followReturnsPastSeeds() {
    return false;
  }

  @Override
  public boolean autoAddZero() {
    return true;
  }

  @Override
  public int numThreads() {
    return 1;
  }

  @Override
  public boolean computeValues() {
    return true;
  }

  @Override
  public boolean recordEdges() {
    return true;
  }

  private static class AliasFlowFunction implements FlowFunction<PointsTo> {
    private final GimpleExpr lhs;
    private final GimpleExpr rhs;

    public AliasFlowFunction(GimpleExpr lhs, GimpleExpr rhs) {
      this.rhs = rhs;
      this.lhs = lhs;
    }

    @Override
    public Set<PointsTo> computeTargets(PointsTo source) {

      if (source.getExpr().equals(rhs)) {
        // source: y = z
        // assignment: x = y
        // Then we know that x = z, (and: y still equals z)
        return Sets.newHashSet(
            source,
            new PointsTo(lhs, source.getAllocation()));

      } else if (source.getExpr().equals(lhs)) {
        // source: x := heap
        // assignment: x = y
        // Then we know that x is being changed and the source fact is no longer relevant.
        return Collections.emptySet();

      } else {
        // No effect, the source fact remains valid
        return Collections.singleton(source);
      }
    }
  }

  private static class AllocFlowFunction implements FlowFunction<PointsTo> {
    private final GimpleExpr lhs;
    private final Allocation allocation;

    public AllocFlowFunction(GimpleExpr lhs, Allocation allocation) {
      this.lhs = lhs;
      this.allocation = allocation;
    }

    @Override
    public Set<PointsTo> computeTargets(PointsTo source) {
      if(source == PointsTo.ZERO) {
        // New knowledge, return the link between the lefthand-side
        // and this malloc.
        return Sets.newHashSet(new PointsTo(lhs, allocation));

      } else if(source.getExpr().equals(lhs)) {
        // source: p=malloc
        // statement: p=x
        // therefore, this statement kills the previous definition.
        return Collections.emptySet();

      } else {
        // This statement has no effect on the source fact.
        return Collections.singleton(source);
      }
    }
  }
}
