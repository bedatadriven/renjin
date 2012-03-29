package org.renjin.compiler.ir.tac.expressions;

/**
 * A SimpleExpression is non recursive: it does not have
 * any "child" operations like a function call does.
 */
public interface SimpleExpression extends Expression {

  SimpleExpression replaceVariable(Variable name, Variable newName);
}
