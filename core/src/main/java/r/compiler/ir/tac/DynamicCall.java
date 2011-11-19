package r.compiler.ir.tac;

import java.util.List;

import javax.sql.rowset.Joinable;

import com.google.common.base.Joiner;

import r.lang.Symbol;

/**
 * Function call that is invoked with the full R
 * flexibility, no assumptions are made...
 */
public class DynamicCall implements Expr {

  private final Symbol name;
  private final List<Expr> arguments;
  
  public DynamicCall(Symbol name, List<Expr> arguments) {
    this.name = name;
    this.arguments = arguments;
  }

  public Symbol getName() {
    return name;
  }

  public List<Expr> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return "dynamic<" + name + ">(" + Joiner.on(", ").join(arguments) + ")";
  }
  
  
}
