/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.ConstantBytecode;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.reflection.converters.BooleanArrayConverter;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Call to a builtin with constant arguments whose value is known at compile-time.
 */
public class ConstantCall implements Specialization {
  private Object constantValue;
  private Type type;
  private ValueBounds valueBounds;
  
  public ConstantCall(Object constantValue) {
    this.constantValue = constantValue;

    if (constantValue instanceof Integer) {
      type = Type.INT_TYPE;
      valueBounds = ValueBounds.of(IntVector.valueOf((Integer) constantValue));
    
    } else if(constantValue instanceof Double) {
      type = Type.DOUBLE_TYPE;
      valueBounds = ValueBounds.of(DoubleVector.valueOf((Double) constantValue));

    } else if(constantValue instanceof Boolean) {
      type = Type.BOOLEAN_TYPE;
      valueBounds = ValueBounds.of(LogicalVector.valueOf((Boolean) constantValue));

    } else if(constantValue instanceof String) {
      type = Type.getType(String.class);
      valueBounds = ValueBounds.of(StringVector.valueOf((String) constantValue));

    } else if(constantValue instanceof SEXP) {
      type = Type.getType(constantValue.getClass());
      valueBounds = ValueBounds.of((SEXP)constantValue);
      
    } else {
      throw new UnsupportedOperationException("constantValue: " + constantValue);
    }
  }

  @Override
  public Type getType() {
    return type;
  }

  public ValueBounds getResultBounds() {
    return valueBounds;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    if(constantValue instanceof Integer) {
      mv.iconst((Integer) constantValue);
    } else if(constantValue instanceof Double) {
      mv.dconst((Double) constantValue);
    } else if(constantValue instanceof Boolean) {
      mv.iconst(constantValue == Boolean.TRUE ? 1 : 0);
    } else if(constantValue instanceof SEXP) {
      ConstantBytecode.pushConstant(mv, ((SEXP) constantValue));
    } else {
      throw new UnsupportedOperationException("constantValue: " + constantValue.getClass());
    }
  }

  @Override
  public boolean isPure() {
    return true;
  }

  public static ConstantCall evaluate(JvmMethod method, List<ValueBounds> arguments) {


    ListVector.Builder varArgs = null;
    Map<String, Object> namedFlags = null;

    if(method.acceptsArgumentList()) {
      namedFlags = new HashMap<>();
      varArgs = ListVector.newBuilder();
      for (JvmMethod.Argument formal : method.getFormals()) {
        if(formal.isNamedFlag()) {
          namedFlags.put(formal.getName(), formal.getDefaultValue());
        }
      }
      for (ValueBounds argument : arguments) {
        varArgs.add(argument.getConstantValue());
      }
    }
    
    List<JvmMethod.Argument> formals = method.getAllArguments();
    Object[] args = new Object[formals.size()];
    Iterator<ValueBounds> it = arguments.iterator();
    int argI = 0;
    for (JvmMethod.Argument formal : formals) {
      if(formal.isVarArg()) {
        args[argI++] = varArgs.build();
      } else if(formal.isNamedFlag()) {
        args[argI++] = namedFlags.get(formal.getName());
      } else if(formal.isContextual()) {
        throw new UnsupportedOperationException("in " + method +  ", " + "formal: " + formal);
      } else {
        ValueBounds argument = it.next();
        Class formalType = formal.getClazz();
        if(formalType.equals(double.class)) {
          args[argI++] = argument.getConstantValue().asReal();
        } else if(formalType.equals(int.class)) {
          args[argI++] = argument.getConstantValue().asInt();
        } else if(SEXP.class.isAssignableFrom(formalType)) {
          args[argI++] = argument.getConstantValue();
        } else {
          throw new UnsupportedOperationException("formal type: " + formalType);
        }
      }
    }

    Object constantValue;
    try {
      constantValue = method.getMethod().invoke(null, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new ConstantCall(constantValue);
  }

  private static void evaluateVarArgs(JvmMethod method, List<ValueBounds> arguments) {
    
    
  }
}
