package org.renjin.gcc.analysis;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Promotes pointer parameters to value parameters.
 * 
 * <p>Fortran passes all parameters as pointers, even if only the value is used. Because pointers
 * are relatively expensive in gcc-bridge-compiled code, we want to eliminate these if possible. </p>
 * 
 */
public class Depointerizer  {

  private CallGraph callGraph;
  private Queue<CallGraph.FunctionNode> workList = Lists.newLinkedList();
  

  public Depointerizer(CallGraph callGraph) {
    this.callGraph = callGraph;
  }
  
  public void run() {
    workList.addAll(callGraph.getFunctionNodes());
    while(!workList.isEmpty()) {
      transform(workList.poll());
    }
  }

  public boolean transform(CallGraph.FunctionNode functionNode) {

    GimpleFunction fn = functionNode.getFunction();
    
    // Build a blacklist of parameters that CANNOT be de-pointerized.
    // A pointer paramater qualifies for the blacklist IF it is used
    // in any way EXCEPT to dereference it.

    Set<Integer> blacklist = Sets.newHashSet();

    
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        for (GimpleExpr operand : statement.getOperands()) {
          checkUse(blacklist, operand);
        }
        if(statement instanceof GimpleAssignment) {
          checkLhs(blacklist, ((GimpleAssignment) statement).getLHS());
        } else if(statement instanceof GimpleCall) {
          checkLhs(blacklist, ((GimpleCall) statement).getLhs());
        }
      }
    }
    
    // now identify any pointer parameters that only have their values used
    for (GimpleParameter parameter : fn.getParameters()) {
      if (isPrimitivePointer(parameter) && 
          !blacklist.contains(parameter.getId())) {
        
        depointerize(functionNode, parameter);
      }
    }
    
    return false;
  }

  private void checkLhs(Set<Integer> blacklist, GimpleLValue lhs) {
    checkUse(blacklist, lhs);
    // On the left hand side of the equation, an statement like
    // *x = 4 means that we can't de-pointerize x
    if(lhs instanceof GimpleMemRef) {
      checkUse(blacklist, ((GimpleMemRef) lhs).getPointer());
    }
  }

  /**
   * Transforms a primitive pointer parameter to a simple primitive parameter.
   * @param fn the function containing the parameter to be transformed
   * @param parameter the parameter to be transformed
   */
  private void depointerize(CallGraph.FunctionNode fn, final GimpleParameter parameter) {
    
    System.out.println("Depointerizing " + parameter.getName() + " in " + fn.getFunction().getName());
    
    // change the type of the parameter, get rid of the pointer type
    parameter.setType(parameter.getType().getBaseType());
    
    // now unwrap all the GimpleMemRefs to this parameter

    IsParameterMemRef predicate = new IsParameterMemRef(parameter);

    for (GimpleBasicBlock basicBlock : fn.getFunction().getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(statement instanceof GimpleAssignment) {
          GimpleAssignment assignment = (GimpleAssignment) statement;
          if(assignment.replace(predicate, new GimpleParamRef(parameter))) {
            assignment.setOperator(GimpleOp.NOP_EXPR);
          }
        } else {
          statement.replaceAll(predicate, new GimpleParamRef(parameter));
        }
      }
    }
    
    // cascade the change to this function's callsites if any
    int parameterIndex = fn.getFunction().getParameters().indexOf(parameter);
    
    for (CallGraph.CallSite callSite : fn.getCallSites()) {
      dereferenceArgument(callSite, parameter, parameterIndex);
    }
  }

  private void dereferenceArgument(CallGraph.CallSite callSite, GimpleParameter parameter, int parameterIndex) {
    List<GimpleExpr> arguments = callSite.getStatement().getOperands();
    
    // Double check that we've got the right type
    GimpleExpr argumentExpr = arguments.get(parameterIndex);
    if(!(argumentExpr.getType() instanceof GimpleIndirectType)) {
      throw new InternalCompilerException("Exception de-pointerizing parameter " + parameter.getName() +
          " in call site " + callSite.getStatement() + ": unexpected parameter type " + argumentExpr.getType());
    }
    
    // Create a temporary variable to hold the dereferenced value
    GimpleFunction callingFunction = callSite.getCallingFunction().getFunction();
    GimpleVarDecl tempVariable = callingFunction.addVarDecl(parameter.getType());
    GimpleAssignment assignment = new GimpleAssignment(GimpleOp.MEM_REF, 
        tempVariable.newRef(), dereference(argumentExpr));
    
    // insert the assignment before the call site
    callSite.insertBefore(assignment);
    
    // and replace the arugment with the temporary variable
    callSite.getStatement().getOperands().set(parameterIndex, tempVariable.newRef());

    // add this callsite to the worklist if not already present
    if(!workList.contains(callSite.getCallingFunction())) {
      workList.add(callSite.getCallingFunction());
    }
  }

  private GimpleExpr dereference(GimpleExpr argumentExpr) {
    if(argumentExpr instanceof GimpleAddressOf) {
      return ((GimpleAddressOf) argumentExpr).getValue();
    } else {
      return new GimpleMemRef(argumentExpr);
    }
  }

  private boolean isPrimitivePointer(GimpleParameter parameter) {
    return parameter.getType() instanceof GimpleIndirectType &&
        parameter.getType().getBaseType() instanceof GimplePrimitiveType;
  }

  /**
   * Checks whether the given {@code expr} is a parameter referenced used in a way that disqualifies
   * it from being de-pointerized.
   */
  private void checkUse(Set<Integer> blacklist, GimpleExpr expr) {
    // If used directly, we can't replace with it's value, so add to the blacklist
    if(expr instanceof GimpleParamRef) {
      blacklist.add(((GimpleParamRef) expr).getId());
    }
    // If derefenced, that's ok, as long as they're not an implicit use of the
    // address with a non-zero offset.
    if(expr instanceof GimpleMemRef) {
      GimpleMemRef memRef = (GimpleMemRef) expr;
      if(!memRef.isOffsetZero()) {
        checkUse(blacklist, ((GimpleMemRef) expr).getPointer());
      }
    }
  }

  private static class IsParameterMemRef implements Predicate<GimpleExpr> {
    private final GimpleParameter parameter;

    public IsParameterMemRef(GimpleParameter parameter) {
      this.parameter = parameter;
    }

    @Override
    public boolean apply(GimpleExpr input) {
      if(input instanceof GimpleMemRef) {
        GimpleExpr pointer = ((GimpleMemRef) input).getPointer();
        if(pointer instanceof GimpleParamRef) {
          GimpleParamRef ref = (GimpleParamRef) pointer;
          if(ref.getId() == parameter.getId()) {
            return true;
          }
        }
      }
      return false;
    }
  }
}
