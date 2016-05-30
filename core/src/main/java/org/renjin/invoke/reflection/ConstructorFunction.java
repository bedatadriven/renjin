package org.renjin.invoke.reflection;

import org.renjin.eval.Context;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
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
      ExternalPtr externalPtr = new ExternalPtr(instance);
      for(Symbol propertyName : propertyValues.keySet()) {
        externalPtr.setMember(propertyName, propertyValues.get(propertyName));
      }
      return externalPtr;
    }
  }
}
