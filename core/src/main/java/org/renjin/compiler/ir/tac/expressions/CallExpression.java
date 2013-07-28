package org.renjin.compiler.ir.tac.expressions;

import java.util.List;

public interface CallExpression extends Expression {

  List<String> getArgumentNames();
}
