package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.compiler.NotCompilableException;
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
  private Class type;

  
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

    if(overloads.size() != 1) {
      throw new UnsupportedOperationException();
    }
    JvmMethod method = overloads.get(0);

    // push all the arguments onto the stack
    for(Expression argument : arguments) {
      argument.emitPush(emitContext, mv);
    }

    // now invoke the method
    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
      Type.getInternalName(method.getDeclaringClass()),
      method.getName(),
      Type.getMethodDescriptor(method.getMethod()));
  }

  @Override
  public Class getType() {
    return type;
  }

  @Override
  public void resolveType() {
    type = overloads.get(0).getReturnType();

    // make sure all overloads return the same
    for(JvmMethod overload : overloads) {
      if(!overload.getReturnType().equals(type)) {
        throw new UnsupportedOperationException("return types are different: " + overloads);
      }
    }
  }

  @Override
  public boolean isTypeResolved() {
    return type != null;
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
