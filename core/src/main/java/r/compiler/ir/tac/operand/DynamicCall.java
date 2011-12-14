package r.compiler.ir.tac.operand;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sql.rowset.Joinable;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import r.compiler.ir.tac.IRClosure;
import r.compiler.ir.tac.IRFunction;
import r.lang.Context;
import r.lang.Environment;
import r.lang.Function;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.PrimitiveFunction;
import r.lang.Promise;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

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
  public Object retrieveValue(Context context, Object[] temps) {
    
    // locate function object
    Function function = findFunction(name, context);
    
    // build argument list 
    PairList.Builder argList = new PairList.Builder();
    for(Operand operand : arguments) {
      argList.add((SEXP)operand.retrieveValue(context, temps));
    }
    PairList args = argList.build();
    FunctionCall call = new FunctionCall(name, args);
    
    return function.apply(context, context.getEnvironment(), call, args);
        
  }
  
  private Function findFunction(SEXP functionExp, Context context) {
    if(functionExp instanceof Symbol) {
      Symbol symbol = (Symbol) functionExp;
      Function fn = context.getEnvironment().findFunction(symbol);
      if(fn == null) {
        throw new EvalException("could not find function '%s'", symbol.getPrintName());      
      }
      return fn;
    } else {
      throw new UnsupportedOperationException("only symbols are supported in function calls right now");
    }
  }


  @Override
  public Set<Variable> variables() {
    Set<Variable> variables = Sets.newHashSet();
    for(Operand operand : arguments) {
      variables.addAll(operand.variables());
    }
    return Collections.unmodifiableSet(variables);
  }

  @Override
  public String toString() {
    String statement;
    if(name.getPrintName().equals(">") || name.getPrintName().equals("<")) {
      statement = "dynamic< " + name + " >";
    } else {
      statement = "dynamic<" + name + ">";
    }
    return statement + "(" + Joiner.on(", ").join(arguments) + ")";
  }  
}
