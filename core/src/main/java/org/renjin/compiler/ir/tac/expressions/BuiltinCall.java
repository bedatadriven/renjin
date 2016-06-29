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
  
  private Object constantValue = null;

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
    
    if(constantValue != null) {
      return generateConstant(mv, constantValue);
    }
    
    if(selectedOverload == null) {
      throw new UnsupportedOperationException();
    }

    if(selectedOverload.isDataParallel()) {
      int resultLength = computeLengths(selectedOverload, argumentTypes);
      if(resultLength == 1) {
        return generateScalarCall(emitContext, mv);
      } else {
        throw new UnsupportedOperationException("non-scalar " + primitive.name + " not implemented");
      }
    } else {
      return generateCall(emitContext, mv);
    }
  }

  private int generateConstant(InstructionAdapter mv, Object constantValue) {
    if(constantValue instanceof Integer) {
      mv.iconst((Integer) constantValue);
      return 1;
    } else if(constantValue instanceof Double) {
      mv.dconst((Double) constantValue);
      return 1;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private int generateCall(EmitContext emitContext, InstructionAdapter mv) {

    Iterator<Expression> it = arguments.iterator();
    
    for (JvmMethod.Argument argument : selectedOverload.getAllArguments()) {
      if(argument.isContextual()) {
        throw new UnsupportedOperationException();
      } else if(argument.isVarArg()) {
        throw new UnsupportedOperationException();
      } else if(argument.isNamedFlag()) {
        throw new UnsupportedOperationException();
      } else {
        Expression positionalArg = it.next();
        positionalArg.load(emitContext, mv);
        emitContext.convert(mv, positionalArg.getType(), Type.getType(argument.getClazz()));
      }
    }
    invokeOverload(mv);
    return 5;
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
    
    if(primitive.name.equals("+")) {
      mv.add(Type.getType(selectedOverload.getPositionalFormals().get(0).getClazz()));
    } else if(primitive.name.equals("/")) {
      mv.div(Type.getType(selectedOverload.getPositionalFormals().get(0).getClazz()));
    } else if(primitive.name.equals("*")) {
      mv.mul(Type.getType(selectedOverload.getPositionalFormals().get(0).getClazz()));
    } else {
      invokeOverload(mv);
    }
    
    return stackHeight;
  }

  private void invokeOverload(InstructionAdapter mv) {
    mv.invokestatic(Type.getInternalName(selectedOverload.getDeclaringClass()), selectedOverload.getName(),
        Type.getMethodDescriptor(selectedOverload.getMethod()), false);
  }


  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    constantValue = null;
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
    if(constantValue != null) {
      if(constantValue instanceof Integer) {
        return Type.INT_TYPE;
      } else if(constantValue instanceof Double) {
        return Type.DOUBLE_TYPE;
      } else {
        throw new UnsupportedOperationException("TODO: " + constantValue);
      }
    }
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
      if(overload.getReturnType().isPrimitive() && allAreConstant(argumentTypes)) {
        // Actually compute the value now rather than at runtime
        return evaluate(argumentTypes);
      } else {
        return ValueBounds.of(overload.getReturnType());
      }
    }
  }

  private ValueBounds evaluate(List<ValueBounds> argumentTypes) {
    List<JvmMethod.Argument> formals = selectedOverload.getAllArguments();
    Object[] args = new Object[formals.size()];
    for (int i = 0; i < formals.size(); i++) {
      selectedOverload.getAllArguments();
    }
    Iterator<ValueBounds> it = argumentTypes.iterator();
    int argI = 0;
    for (JvmMethod.Argument formal : formals) {
      if(formal.isContextual() || formal.isVarArg() || formal.isNamedFlag()) {
        throw new UnsupportedOperationException("formal: " + formal);
      }
      ValueBounds argument = it.next();
      args[argI++] = argument.getConstantValue();
    }

    try {
      constantValue = selectedOverload.getMethod().invoke(null, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return valueBounds.of(constantValue);
  }

  private boolean allAreConstant(List<ValueBounds> argumentTypes) {
    for (ValueBounds argumentType : argumentTypes) {
      if(!argumentType.isConstant()) {
        return false;
      }
    }
    return true;
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
