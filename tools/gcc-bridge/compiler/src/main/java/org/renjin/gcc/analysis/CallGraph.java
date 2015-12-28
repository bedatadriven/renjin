package org.renjin.gcc.analysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Mapping between functions and their call sites
 */
public class CallGraph {

  public class FunctionNode {
    private final GimpleCompilationUnit unit;
    private final GimpleFunction function;
    private final List<CallSite> callSites = Lists.newArrayList();

    public FunctionNode(GimpleCompilationUnit unit, GimpleFunction function) {
      this.unit = unit;
      this.function = function;
    }

    public GimpleFunction getFunction() {
      return function;
    }

    public List<CallSite> getCallSites() {
      return callSites;
    }
  }

  public class CallSite {
    private final FunctionNode callingFunction;
    private final GimpleBasicBlock basicBlock;
    private final GimpleCall statement;

    public CallSite(FunctionNode callingFunction, GimpleBasicBlock basicBlock, GimpleCall statement) {
      this.callingFunction = callingFunction;
      this.basicBlock = basicBlock;
      this.statement = statement;
    }

    public GimpleCall getStatement() {
      return statement;
    }

    public FunctionNode getCallingFunction() {
      return callingFunction;
    }

    public void insertBefore(GimpleStatement newStatement) {
      int callIndex = basicBlock.getStatements().indexOf(statement);
      basicBlock.getStatements().add(callIndex, newStatement);
    }
  }

  private List<FunctionNode> functionNodes = Lists.newArrayList();
  private List<CallSite> callSites = Lists.newArrayList();


  public CallGraph(Collection<GimpleCompilationUnit> units) {

    Map<String, FunctionNode> globalScope = Maps.newHashMap();
    Map<GimpleCompilationUnit, Map<String, FunctionNode>> unitScopes = Maps.newHashMap();

    // Do a first pass to create initial set of nodes

    for (GimpleCompilationUnit unit : units) {

      Map<String, FunctionNode> unitScope = Maps.newHashMap();

      for (GimpleFunction function : unit.getFunctions()) {
        FunctionNode node = new FunctionNode(unit, function);
        unitScope.put(function.getName(), node);
        if(function.isExtern()) {
          globalScope.put(function.getName(), node);
        }
        functionNodes.add(node);
      }

      unitScopes.put(unit, unitScope);
    }

    // Now a second pass to link call sites to their functions
    for (FunctionNode callingNode : functionNodes) {
      Map<String, FunctionNode> unitScope = unitScopes.get(callingNode.unit);

      for (GimpleBasicBlock basicBlock : callingNode.getFunction().getBasicBlocks()) {
        for (GimpleStatement statement : basicBlock.getStatements()) {
          if(statement instanceof GimpleCall) {
            CallSite callSite = new CallSite(callingNode, basicBlock, ((GimpleCall) statement));
            FunctionNode functionNode = findFunction(((GimpleCall) statement), unitScope, globalScope);
            if(functionNode != null) {
              functionNode.callSites.add(callSite);
            }
            callSites.add(callSite);
          }
        }
      }
    }
  }

  public List<FunctionNode> getFunctionNodes() {
    return functionNodes;
  }

  public List<CallSite> getCallSites() {
    return callSites;
  }

  private FunctionNode findFunction(GimpleCall statement, Map<String, FunctionNode> unitScope, Map<String, FunctionNode> globalScope) {
    GimpleExpr functionExpr = statement.getFunction();
    if(functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf functionPtr = (GimpleAddressOf) functionExpr;
      if(functionPtr.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) functionPtr.getValue();
        FunctionNode node = unitScope.get(ref.getName());
        if(node == null) {
          return globalScope.get(ref.getName());
        }
        return node;
      }
    }
    return null;
  }

}
