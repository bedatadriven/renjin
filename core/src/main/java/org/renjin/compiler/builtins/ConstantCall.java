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

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;

import java.util.Iterator;
import java.util.List;

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
    
    } else {
      throw new UnsupportedOperationException("constantValue: " + constantValue);
    }
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    if(constantValue instanceof Integer) {
      mv.iconst((Integer) constantValue);
    } else if(constantValue instanceof Double) {
      mv.dconst((Double) constantValue);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public static ConstantCall evaluate(JvmMethod method, List<ValueBounds> arguments) {
    List<JvmMethod.Argument> formals = method.getAllArguments();
    Object[] args = new Object[formals.size()];
    for (int i = 0; i < formals.size(); i++) {
      method.getAllArguments();
    }
    Iterator<ValueBounds> it = arguments.iterator();
    int argI = 0;
    for (JvmMethod.Argument formal : formals) {
      if(formal.isContextual() || formal.isVarArg() || formal.isNamedFlag()) {
        throw new UnsupportedOperationException("formal: " + formal);
      }
      ValueBounds argument = it.next();
      args[argI++] = argument.getConstantValue();
    }

    Object constantValue;
    try {
      constantValue = method.getMethod().invoke(null, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new ConstantCall(constantValue);
  }
}
