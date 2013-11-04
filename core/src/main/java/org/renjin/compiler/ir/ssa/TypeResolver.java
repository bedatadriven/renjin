package org.renjin.compiler.ir.ssa;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.Map;
import java.util.Queue;

public class TypeResolver {

  private final ControlFlowGraph cfg;
  private VariableMap variableMap;
  private boolean changes;

  public TypeResolver(ControlFlowGraph cfg, VariableMap variableMap) {
    this.cfg = cfg;
    this.variableMap = variableMap;
  }


  public void resolveTypes() {
    do {
      // keep passing over the tree until everything is resolved
      changes = false;
      System.out.println("Resolve: starting iteration...");

      for(BasicBlock bb : cfg.getBasicBlocks()) {
        if(bb != cfg.getExit()) {
          for(Statement stmt : bb.getStatements()) {
            resolveTypes(stmt.getRHS());

            if(stmt instanceof Assignment) {
              LValue lhs = ((Assignment) stmt).getLHS();
              resolveTypes(lhs);
            }
          }
        }
      }
    } while(changes);
  }

  // do a depth-first search to find unresolved types
  private boolean resolveTypes(Expression expr) {

    boolean unresolved = false;
    for(int i=0;i!=expr.getChildCount();++i) {
      Expression child = expr.childAt(i);
      if(!resolveTypes(child)) {
        unresolved = true;
      }
    }

    if(unresolved) {
      return false;
    } else {
      return resolveType(expr);
    }
  }

  private boolean resolveType(Expression rhs) {
    if(rhs.isTypeResolved()) {
      return true;
    } else {
      System.out.println("trying to resolve: " + rhs);
      if(rhs instanceof LValue) {
        return resolveLValue((LValue)rhs);
      } else {
        rhs.resolveType();
        changes = true;
        return true;
      }
    }
  }

  private boolean resolveLValue(LValue var) {
    Expression definition = variableMap.getDefinition(var);
    if(definition == null) {
      // we end up with a lot more phi nodes than we need...
      // have to investigate whether this is a problem with our
      // ssa transformer or just an expected artifact of the process
      System.err.println("No definition for " + var);
      return false;
    }
    if(definition.isTypeResolved()) {
      var.setType(definition.getType());
      System.out.println(var + "=>" + definition.getType());
      changes = true;
      return true;
    } else {
      return false;
    }
  }
}
