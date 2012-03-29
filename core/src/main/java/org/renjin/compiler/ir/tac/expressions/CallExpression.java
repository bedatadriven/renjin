package org.renjin.compiler.ir.tac.expressions;

import java.util.List;

public interface CallExpression extends Expression {
  
  int getElipsesIndex();
  boolean hasElipses();
  List<String> getArgumentNames();
}
