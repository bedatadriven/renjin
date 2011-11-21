package r.jvmi.r2j;

import java.util.List;
import java.util.Map;

import r.jvmi.wrapper.ArgumentIterator;
import r.lang.AbstractSEXP;
import r.lang.Context;
import r.lang.Environment;
import r.lang.Function;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.SexpVisitor;
import r.lang.Symbol;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ConstructorFunction extends AbstractSEXP implements Function {

  private final ConstructorBinding binding;
  
  public ConstructorFunction(ConstructorBinding binding) {
    super();
    this.binding = binding;
  }

  @Override
  public String getTypeName() {
    return "constructor";
  }

  @Override
  public void accept(SexpVisitor visitor) {
    
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call,
      PairList args) {
    
    List<SEXP> constructorArgs = Lists.newArrayList();
    Map<Symbol, SEXP> propertyValues = Maps.newHashMap();
    
    ArgumentIterator argIt = new ArgumentIterator(context, rho, args);
    while(argIt.hasNext()) {
      PairList.Node node = argIt.nextNode();
      SEXP evaled = node.getValue().evaluate(context, rho);
      
      if(node.hasTag()) {
        propertyValues.put(node.getTag(), evaled);
      } else {
        constructorArgs.add(evaled);
      }
    }
  
    Object instance = binding.newInstance(constructorArgs);
    Environment env = Environment.createChildEnvironment(Environment.EMPTY,
        new ObjectFrame(instance));
    
    for(Map.Entry<Symbol, SEXP> property : propertyValues.entrySet()) {
      env.setVariable(property.getKey(), property.getValue());
    }
    
    return env;
  }
}
