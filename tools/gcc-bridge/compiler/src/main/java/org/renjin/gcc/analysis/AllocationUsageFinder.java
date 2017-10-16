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
import heros.*;
import heros.flowfunc.Identity;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleParamRef;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.repackaged.guava.cache.LoadingCache;
import soot.Local;
import soot.Type;
import soot.Value;
import soot.toolkits.scalar.Pair;

import java.util.*;

public class AllocationUsageFinder implements IFDSTabulationProblem<
    GimpleNode,
    AllocationFact,
    GimpleFunction,
    GimpleInterproceduralCFG> {

  private final GimpleInterproceduralCFG cfg;

  public AllocationUsageFinder(GimpleInterproceduralCFG cfg) {
    this.cfg = cfg;
  }

  @Override
  public FlowFunctions<GimpleNode, AllocationFact, GimpleFunction> flowFunctions() {
    return new FlowFunctions<GimpleNode, AllocationFact, GimpleFunction>() {
      @Override
      public FlowFunction<AllocationFact> getNormalFlowFunction(final GimpleNode curr, GimpleNode succ) {

        if(Malloc.isMalloc(curr.getStatement())) {
          final Allocation allocation = new MallocAllocation(curr.getStatement());
          final GimpleExpr lvalue = ((GimpleCall) curr.getStatement()).getLhs();

          return new FlowFunction<AllocationFact>() {
            @Override
            public Set<AllocationFact> computeTargets(AllocationFact source) {
              if(source == AllocationFact.ZERO) {
                return Sets.newHashSet(new AllocationFact(lvalue, allocation));
              } else if(source.getExpr().equals(lvalue)) {
                // strong update for local variables??
                return Collections.emptySet();
              } else {
                return Collections.singleton(source);
              }
            }
          };
        }

        return new FlowFunction<AllocationFact>() {
          @Override
          public Set<AllocationFact> computeTargets(AllocationFact source) {
            throw new UnsupportedOperationException("TODO: " + curr.getStatement());
          }
        };
      }

      @Override
      public FlowFunction<AllocationFact> getCallFlowFunction(final GimpleNode callStmt, GimpleFunction destinationMethod) {
        GimpleCall call = (GimpleCall) callStmt.getStatement();
        final List<GimpleExpr> callArgs = call.getOperands();
        final List<GimpleParamRef> paramLocals = new ArrayList<>();
        for (int i = 0; i < destinationMethod.getParameters().size(); i++) {
          paramLocals.add(new GimpleParamRef(destinationMethod.getParameters().get(i)));
        }
        return new FlowFunction<AllocationFact>() {
          public Set<AllocationFact> computeTargets(AllocationFact source) {
            GimpleExpr value = source.getExpr();
            int argIndex = callArgs.indexOf(value);
            if (argIndex > -1) {
              return Collections.singleton(new AllocationFact(paramLocals.get(argIndex), source.getAllocation()));
            }
            return Collections.emptySet();
          }
        };
      }

      @Override
      public FlowFunction<AllocationFact> getReturnFlowFunction(GimpleNode callSite, GimpleFunction calleeMethod, GimpleNode exitStmt, GimpleNode returnSite) {
        throw new UnsupportedOperationException("TODO");
      }

      @Override
      public FlowFunction<AllocationFact> getCallToReturnFlowFunction(GimpleNode callSite, GimpleNode returnSite) {
        return Identity.v();
      }
    };
  }

  @Override
  public GimpleInterproceduralCFG interproceduralCFG() {
    return cfg;
  }

  @Override
  public Map<GimpleNode, Set<AllocationFact>> initialSeeds() {
    Map<GimpleNode, Set<AllocationFact>> seeds = new HashMap<>();
    for (GimpleFunction gimpleFunction : cfg.getEntryPoints()) {
      for (GimpleNode startPoint : cfg.getStartPointsOf(gimpleFunction)) {
        seeds.put(startPoint, Collections.singleton(zeroValue()));
      }
    }
    return seeds;
  }

  @Override
  public AllocationFact zeroValue() {
    return AllocationFact.ZERO;
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
}
