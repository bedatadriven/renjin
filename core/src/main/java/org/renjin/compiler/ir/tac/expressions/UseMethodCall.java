package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRMatchedArguments;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.primitives.S3;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.StringVector;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Call to UseMethod
 */
public class UseMethodCall implements Expression {

  private RuntimeState runtimeState;
  private FunctionCall call;
  /**
   * The name of the generic method. 
   */
  private final String generic;

  /**
   * The object expression whose class is used to dispatch the call. 
   */
  private Expression objectExpr;
  private List<IRArgument> arguments;
  
  
  private InlinedFunction inlinedMethod = null;
  private IRMatchedArguments matchedArguments;
  
  private Type type;
  private ValueBounds returnBounds;
  

  public UseMethodCall(RuntimeState runtimeState, FunctionCall call, String generic, Expression objectExpr) {
    this.runtimeState = runtimeState;
    this.call = call;
    this.generic = generic;
    this.objectExpr = objectExpr;
    this.returnBounds = ValueBounds.UNBOUNDED;
    this.type = returnBounds.storageType();    
    
    // Cheating for now because we only support unary functions...
    arguments = Collections.singletonList(new IRArgument(objectExpr));
  }

  @Override
  public boolean isDefinitelyPure() {
    return false;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {

    if(inlinedMethod == null) {
      throw new NotCompilableException(call, "Could not resolve UseMethod() target");
    }
    
    if(matchedArguments.hasExtraArguments()) {
      throw new NotCompilableException(call, "Extra arguments not supported");
    }

    inlinedMethod.writeInline(emitContext, mv, matchedArguments, arguments);
    
    return 0;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {

    ValueBounds objectBounds = typeMap.get(objectExpr);
    StringVector objectClass = S3.computeDataClasses(objectBounds);
    
    if(objectClass != null) {

      // Otherwise, try to resolve the function 
      Function function = runtimeState.findMethod(generic, null, objectClass);
      if(function instanceof Closure) {
        Closure closure = (Closure) function;

        if(inlinedMethod == null || inlinedMethod.getClosure() != function) {
          matchedArguments = new IRMatchedArguments(closure, arguments);
          inlinedMethod = new InlinedFunction(runtimeState, closure, matchedArguments.getSuppliedFormals());
        }

        if(matchedArguments.hasExtraArguments()) {
          throw new NotCompilableException(call, "Extra arguments not supported");
        }

        returnBounds = inlinedMethod.updateBounds(arguments, typeMap);
        type = returnBounds.storageType();
        return returnBounds;
      }
    }

    // if the class of the object is not known, or we can't find a matching
    // generic function, then we can't do much
    this.returnBounds = ValueBounds.UNBOUNDED;
    this.type = returnBounds.storageType();

    return returnBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return returnBounds;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      objectExpr = child;
    } else {
      arguments.get(childIndex - 1).setExpression(child);
    }  
  }

  @Override
  public int getChildCount() {
    return 1 + arguments.size();
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return objectExpr;
    } else {
      return arguments.get(index - 1).getExpression();
    }
  }

  @Override
  public String toString() {
    return "UseMethod(" + generic + ", " + objectExpr + ", " + Joiner.on(", ").join(arguments) + ")";
  }
}
