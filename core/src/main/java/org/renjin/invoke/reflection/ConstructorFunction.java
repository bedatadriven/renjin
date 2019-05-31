/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.invoke.reflection;

import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
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
  public SEXP apply(Context context, Environment rho, FunctionCall call) {

    List<SEXP> constructorArgs = Lists.newArrayList();
    Map<Symbol, SEXP> propertyValues = Maps.newHashMap();
    
    ArgumentIterator argIt = new ArgumentIterator(context, rho, call.getArguments());
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

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch) {
    throw new UnsupportedOperationException("TODO");
  }
}
