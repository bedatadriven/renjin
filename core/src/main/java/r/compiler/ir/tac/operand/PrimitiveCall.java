package r.compiler.ir.tac.operand;

import java.util.List;

import com.google.common.base.Joiner;

import r.lang.Context;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;

/**
 * 
 */
public class PrimitiveCall implements Operand {

  private Symbol name;
  private final List<Operand> arguments;
  
  public PrimitiveCall(Symbol name, List<Operand> arguments) {
    super();
    this.name = name;
    this.arguments = arguments;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    // build argument list 
    PairList.Builder argList = new PairList.Builder();
    for(Operand operand : arguments) {
      argList.add((SEXP)operand.retrieveValue(context, temps));
    }
    
    FunctionCall call = new FunctionCall(name, argList.build());
    return call.evaluate(context, context.getEnvironment());
  }
  
  @Override
  public String toString() {
    String statement;
    if(name.getPrintName().equals(">") || name.getPrintName().equals("<")) {
      statement = "primitive< " + name + " >";
    } else {
      statement = "primitive<" + name + ">";
    }
    return statement + "(" + Joiner.on(", ").join(arguments) + ")";
  }  
}
