package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.math.complex.Complex;
import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.TypeBounds;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.util.*;

/**
 * Call to a builtin function
 */
public class BuiltinCall implements CallExpression {

  private final Primitives.Entry primitive;
  private final String[] argumentNames;
  private final List<Expression> arguments;
  private final List<JvmMethod> methods;

  public BuiltinCall(Primitives.Entry primitive, String[] argumentNames, List<Expression> arguments) {
    this.primitive = primitive;
    this.argumentNames = argumentNames;
    this.arguments = arguments;
    this.methods = JvmMethod.findOverloads(primitive.functionClass, primitive.name, primitive.methodName);

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
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  
  @Override
  public TypeBounds computeTypeBounds(Map<LValue, TypeBounds> variableMap) {
    List<TypeBounds> argumentTypes = new ArrayList<>();
    for (Expression argument : arguments) {
      argumentTypes.add(argument.computeTypeBounds(variableMap));
    }
    
    List<TypeBounds> resultTypes = new ArrayList<>();
    for (JvmMethod method : methods) {
      if (matches(method, argumentTypes)) {
        resultTypes.add(computeTypeBounds(method, argumentTypes));
      }
    }
    return TypeBounds.union(resultTypes);
  }

  private boolean matches(JvmMethod method, List<TypeBounds> argumentTypes) {
    if(!arityMatches(method, argumentTypes)) {
      return false; 
    }
    for (int i = 0; i < method.getPositionalFormals().size(); i++) {
      JvmMethod.Argument formal = method.getPositionalFormals().get(i);
      TypeBounds actualType = argumentTypes.get(i);
      if(!matches(formal, actualType)) {
        return false;
      }
    }
    return true;
  }

  private boolean matches(JvmMethod.Argument formal, TypeBounds actualType) {
    if(formal.getClazz().equals(SEXP.class)) {
      return true;
    }
    if(formal.getClazz().equals(Vector.class)) {
      return actualType.couldBeVector();
    } 
    if(formal.getClazz().equals(AtomicVector.class)) {
      return actualType.couldBeAtomicVector();
    }
    if(formal.getClazz().equals(int.class)) {
      return actualType.couldBe(TypeBounds.INT | TypeBounds.DOUBLE);
    }
    if(formal.getClazz().equals(double.class)) {
      return actualType.couldBe(TypeBounds.DOUBLE);
    }
    if(formal.getClazz().equals(Complex.class)) {
      return actualType.couldBe(TypeBounds.COMPLEX);
    }
    throw new UnsupportedOperationException("formal type: " + formal.getClazz().getName());
  }

  private boolean arityMatches(JvmMethod method, List<TypeBounds> argumentTypes) {
    int numPosArgs = method.getPositionalFormals().size();
    return (argumentTypes.size() == numPosArgs) ||
        (method.acceptsArgumentList() && (argumentTypes.size() >= numPosArgs));
  }

  private TypeBounds computeTypeBounds(JvmMethod overload, List<TypeBounds> argumentTypes) {
    if(overload.isDataParallel()) {
      return TypeBounds.vector(overload.getReturnType(), computeLengths(overload, argumentTypes));
    } else {
      return TypeBounds.UNBOUNDED;
    }
  }

  private Set<Integer> computeLengths(JvmMethod overload, List<TypeBounds> argumentTypes) {
    List<TypeBounds> recycledArguments = Lists.newArrayList();
    for (int i = 0; i < overload.getPositionalFormals().size(); i++) {
      JvmMethod.Argument formal = overload.getPositionalFormals().get(i);
      if(formal.isRecycle()) {
        recycledArguments.add(argumentTypes.get(i));
      }
    }

    if(recycledArguments.size() == 1) {
      return recycledArguments.get(0).getLengthSet();

    } else if(recycledArguments.size() == 2) {
      return computeLengths(recycledArguments.get(0), recycledArguments.get(1));
    
    } else {
      // TODO:
      return null;
    }
  }

  private Set<Integer> computeLengths(TypeBounds x, TypeBounds y) {
    Set<Integer> set1 = x.getLengthSet();
    Set<Integer> set2 = y.getLengthSet();

    // If either set1 or set2 are open, then the result
    // is open
    if(set1 == null || set2 == null) {
      return null;
    }
    
    // Otherwise find the cartesian product of possible lengths
    Set<Integer> set = new HashSet<>();
    for (Integer length1 : set1) {
      for (Integer length2 : set2) {
        if(length1 == 0 || length2 == 0) {
          set.add(0);
        } else {
          set.add(Math.max(length1, length2));
        }
      }
    }
    return set;
  }

  @Override
  public String toString() {
    return "(" + primitive.name + " " + Joiner.on(" ").join(arguments) + ")";
  }
}
