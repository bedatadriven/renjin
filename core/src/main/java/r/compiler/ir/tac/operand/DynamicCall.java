package r.compiler.ir.tac.operand;

import java.util.List;

import javax.sql.rowset.Joinable;

import com.google.common.base.Joiner;

import r.lang.Symbol;

/**
 * Function call that is invoked with the full R
 * flexibility, no assumptions are made...
 */
public class DynamicCall implements Operand {

  private final Symbol name;
  private final List<Operand> arguments;
  
  public DynamicCall(Symbol name, List<Operand> arguments) {
    this.name = name;
    this.arguments = arguments;
  }

  public Symbol getName() {
    return name;
  }

  public List<Operand> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return "dynamic<" + name + ">(" + Joiner.on(", ").join(arguments) + ")";
  }
  
  
}
