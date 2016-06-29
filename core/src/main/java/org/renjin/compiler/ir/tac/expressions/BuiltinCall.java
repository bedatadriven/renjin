package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.invoke.codegen.OverloadComparator;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.SEXP;

import java.util.*;

/**
 * Call to a builtin function
 */
public class BuiltinCall implements CallExpression {

  private final Primitives.Entry primitive;
  private final String[] argumentNames;
  private final List<Expression> arguments;
  private final List<JvmMethod> methods;

  /**
   * The value bounds computed from our arguments
   */
  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;

  /**
   * The selected overload, based on our arguments, or null
   * if it cannot be determined at compile time.
   */
  private JvmMethod selectedOverload = null;
  private List<ValueBounds> argumentTypes;

  public BuiltinCall(Primitives.Entry primitive, String[] argumentNames, List<Expression> arguments) {
    this.primitive = primitive;
    this.argumentNames = argumentNames;
    this.arguments = arguments;
    this.methods = JvmMethod.findOverloads(primitive.functionClass, primitive.name, primitive.methodName);

    Collections.sort( methods, new OverloadComparator());


  }

  @Override
  public List<String> getArgumentNames() {
    return Arrays.asList(argumentNames);
  }

  @Override
  public int getChildCount() {
    return arguments.size();
  }
  
  @Override
  public Expression childAt(int index) {
    return arguments.get(index);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.set(childIndex, child);
  }
  
  @Override
  public boolean isDefinitelyPure() {
    return false;
  }


  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    if(selectedOverload == null) {
      throw new UnsupportedOperationException();
    }

    if(selectedOverload.isDataParallel()) {
      int resultLength = computeLengths(selectedOverload, argumentTypes);
      if(resultLength == 1) {
        return generateScalarCall(emitContext, mv);
      }
    }
    throw new UnsupportedOperationException(); 
  }

  private int generateScalarCall(EmitContext emitContext, InstructionAdapter mv) {
    int stackHeight = 0;
    for (JvmMethod.Argument argument : selectedOverload.getAllArguments()) {
      if(argument.isContextual()) {
        throw new UnsupportedOperationException("TODO");
      } else {
        Expression argumentExpr = arguments.get(argument.getIndex());
        stackHeight += argumentExpr.load(emitContext, mv);
        emitContext.convert(mv, argumentExpr.getType(), Type.getType(argument.getClazz()));
      }
    }
    mv.invokestatic(Type.getInternalName(selectedOverload.getDeclaringClass()), selectedOverload.getName(), 
        Type.getMethodDescriptor(selectedOverload.getMethod()), false);
    
    return stackHeight;
  }


  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    argumentTypes = new ArrayList<>();
    for (Expression argument : arguments) {
      argumentTypes.add(argument.updateTypeBounds(typeMap));
    }
    selectedOverload = selectOverload(argumentTypes);
    if(selectedOverload == null) {
      valueBounds = ValueBounds.UNBOUNDED;
    } else {
      valueBounds = computeTypeBounds(selectedOverload, argumentTypes);
    }
    
    return valueBounds;
  }


  @Override
  public Type getType() {
    if(selectedOverload == null) {
      return Type.getType(SEXP.class);
    } else {
      return Type.getType(selectedOverload.getReturnType());
    }
  }


  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  private JvmMethod selectOverload(List<ValueBounds> argumentTypes) {
    for (JvmMethod method : methods) {
      if(matches(method, argumentTypes)) {
        return method;
      }
    }
    return null;
  }

  private boolean matches(JvmMethod method, List<ValueBounds> argumentTypes) {
    if(!arityMatches(method, argumentTypes)) {
      return false; 
    }
    for (int i = 0; i < method.getPositionalFormals().size(); i++) {
      JvmMethod.Argument formal = method.getPositionalFormals().get(i);
      ValueBounds actualType = argumentTypes.get(i);
      
      if(!TypeSet.matches(formal.getClazz(), actualType.getTypeSet())) {
        return false;
      }
    }
    return true;
  }

  private boolean arityMatches(JvmMethod method, List<ValueBounds> argumentTypes) {
    int numPosArgs = method.getPositionalFormals().size();
    return (argumentTypes.size() == numPosArgs) ||
        (method.acceptsArgumentList() && (argumentTypes.size() >= numPosArgs));
  }

  private ValueBounds computeTypeBounds(JvmMethod overload, List<ValueBounds> argumentTypes) {
    if(overload.isDataParallel()) {
      return ValueBounds.vector(overload.getReturnType(), computeLengths(overload, argumentTypes));
    } else {
      return ValueBounds.UNBOUNDED;
    }
  }

  private int computeLengths(JvmMethod overload, List<ValueBounds> argumentTypes) {
    List<ValueBounds> recycledArguments = Lists.newArrayList();
    for (int i = 0; i < overload.getPositionalFormals().size(); i++) {
      JvmMethod.Argument formal = overload.getPositionalFormals().get(i);
      if(formal.isRecycle()) {
        recycledArguments.add(argumentTypes.get(i));
      }
    }
    
    int maxLength = 1;
    for (ValueBounds recycledArgument : recycledArguments) {
      if(recycledArgument.getLength() == ValueBounds.UNKNOWN_LENGTH) {
        return ValueBounds.UNKNOWN_LENGTH;
      } else if(recycledArgument.getLength() == 0) {
        return 0;
      } else if (recycledArgument.getLength() > maxLength) {
        maxLength = recycledArgument.getLength();
      }
    }
    return maxLength;
  }


  @Override
  public String toString() {
    return "(" + primitive.name + " " + Joiner.on(" ").join(arguments) + ")";
  }
}
