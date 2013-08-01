package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.invoke.model.JvmMethod;

import java.util.Arrays;
import java.util.List;

/**
 * Expression which targets a set of JVM methods
 */
public class JvmMethodCall implements CallExpression {

  private final String name;
  private final List<Expression> arguments;
  private List<JvmMethod> overloads;
  private String[] argumentNames;

  
  public JvmMethodCall(String name, List<JvmMethod> overloads, String[] argumentNames, List<Expression> arguments) {
    super();
    this.name = name;
    this.arguments = arguments;
    this.argumentNames = argumentNames;
    this.overloads = overloads;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return "(" + name + " " + Joiner.on(" ").join(arguments) + ")";
  }

  @Override
  public boolean isDefinitelyPure() {
    for(JvmMethod overload : overloads) {
      if(!overload.isDeferrable()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class inferType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.set(childIndex, child);
  }

  @Override
  public int getChildCount() {
    return arguments.size();
  }

  @Override
  public Expression childAt(int index) {
    return arguments.get(index);
  }

  public List<String> getArgumentNames() {
    return Arrays.asList(argumentNames);
  }
}
