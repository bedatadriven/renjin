package org.renjin.jvminterop;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.eval.Context;
import org.renjin.primitives.annotations.processor.ArgumentIterator;
import org.renjin.sexp.*;

import java.util.List;
import java.util.Map;

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
      SEXP evaled = context.evaluate( node.getValue(), rho);
      
      if(node.hasTag()) {
        propertyValues.put(node.getTag(), evaled);
      } else {
        constructorArgs.add(evaled);
      }
    }
  
    Object instance = binding.newInstance(context, constructorArgs);
    if(instance instanceof SEXP) {
      return (SEXP) instance;
    } else {
      Environment env = Environment.createChildEnvironment(Environment.EMPTY,
          new ObjectFrame(instance));

      for(Map.Entry<Symbol, SEXP> property : propertyValues.entrySet()) {
        env.setVariable(property.getKey(), property.getValue());
      }

      return env;
    }
  }
}
