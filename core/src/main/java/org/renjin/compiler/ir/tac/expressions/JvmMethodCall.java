package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.invoke.model.JvmMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    String statement;
    if(name.equals(">") || name.equals("<")) {
      statement = "primitive< " + name + " >";
    } else {
      statement = "primitive<" + name + ">";
    }
    return statement + "(" + Joiner.on(", ").join(arguments) + ")";
  }

  @Override
  public Set<Variable> variables() {
    Set<Variable> variables = Sets.newHashSet();
    for(Expression operand : arguments) {
      variables.addAll(operand.variables());
    }
    return Collections.unmodifiableSet(variables);
  }

  @Override
  public JvmMethodCall replaceVariable(Variable name, Variable newName) {
    List<Expression> newOps = Lists.newArrayListWithCapacity(arguments.size());
    for(Expression argument : arguments) {
      newOps.add(argument.replaceVariable(name, newName));
    }
    return new JvmMethodCall(this.name, overloads, argumentNames, newOps);
  }

  @Override
  public List<Expression> getChildren() {
    return arguments;
  }

  @Override
  public void setChild(int i, Expression expr) {
    arguments.set(i, expr);
  }

  public List<String> getArgumentNames() {
    return Arrays.asList(argumentNames);
  }

}
