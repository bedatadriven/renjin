/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.PrimitiveFunction;

import java.util.Map;

public class FunctionCallTranslators {

  private Map<String, FunctionCallTranslator> specials = Maps.newHashMap();
  private Map<String, FunctionCallTranslator> builtins = Maps.newHashMap();

  public FunctionCallTranslators() {
    specials.put("if", new IfTranslator());
    specials.put("{", new BracketTranslator());
    specials.put("(", new ParenTranslator());
    specials.put("<-", new AssignLeftTranslator());
    specials.put("<<-", new ReassignLeftTranslator());
    specials.put("=", new AssignTranslator());
    specials.put("for", new ForTranslator());
    specials.put("repeat", new RepeatTranslator());
    specials.put("while", new WhileTranslator());
    specials.put("next", new NextTranslator());
    specials.put("break", new BreakTranslator());
    specials.put("function", new ClosureDefinitionTranslator());
    specials.put("$", new DollarTranslator());
    specials.put("$<-", new DollarAssignTranslator());
    specials.put(".Internal", new InternalCallTranslator());
    specials.put("&&", new AndTranslator());
    specials.put("||", new OrTranslator());
    specials.put("switch", new SwitchTranslator());
    specials.put("quote", new QuoteTranslator());
    specials.put("return", new ReturnTranslator());
    specials.put(":", new SequenceTranslator());
    specials.put("UseMethod", new UseMethodTranslator());
    specials.put("@", new SlotTranslator());
    specials.put("missing", new MissingTranslator());
  }
  
  public FunctionCallTranslator get(Function function) {
    if(function instanceof PrimitiveFunction) {
      PrimitiveFunction primitiveFunction = (PrimitiveFunction)function;
      if(specials.containsKey(primitiveFunction.getName())) {
        return specials.get(primitiveFunction.getName());
      } else {
        return BuiltinTranslator.INSTANCE;
      }
    }
  
    if(function instanceof Closure) {
      return new ClosureCallTranslator((Closure) function);
    }
    
    throw new NotCompilableException(function, "Can't handle functions of class " + function.getClass().getName());
  }
}
