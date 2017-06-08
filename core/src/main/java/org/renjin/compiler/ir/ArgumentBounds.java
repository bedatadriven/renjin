package org.renjin.compiler.ir;

import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by parham on 8-6-17.
 */
public class ArgumentBounds {
  private String name;
  private ValueBounds valueBounds;
  
  
  public ArgumentBounds(String name, ValueBounds valueBounds) {
    this.name = name;
    this.valueBounds = valueBounds;
  }
  
  public boolean isNamed() {
    return name != null;
  }
  
  public String getName() {
    return name;
  }
  
  public ValueBounds getValueBounds() {
    return valueBounds;
  }
  
  
  public void setValueBounds(ValueBounds valueBounds) {
    this.valueBounds = valueBounds;
  }
  
  public static boolean anyNamed(Iterable<IRArgument> arguments) {
    for (IRArgument argument : arguments) {
      if(argument.isNamed()) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    if(isNamed()) {
      return name + " = " + valueBounds;
    } else {
      return valueBounds.toString();
    }
  }
}
