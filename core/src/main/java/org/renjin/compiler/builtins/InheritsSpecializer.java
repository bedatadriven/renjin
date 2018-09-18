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
package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Attributes;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.sexp.*;

import java.util.List;

public class InheritsSpecializer implements BuiltinSpecializer {

  private final JvmMethod fallback;

  public InheritsSpecializer() {
    fallback = Iterables.getOnlyElement(JvmMethod.findOverloads(Attributes.class, "inherits", "inherits"));
  }

  @Override
  public String getName() {
    return "inherits";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
    InvalidSyntaxException.checkInternalArity(getName(), 3, arguments.size());
    ArgumentBounds x = arguments.get(0);
    ArgumentBounds what = arguments.get(1);
    ArgumentBounds which = arguments.get(2);

    // The 'which' flag determines whether we return an integer array (TRUE) or a boolean flag (FALSE)
    if(which.getBounds().isConstantFlagEqualTo(false)) {
      if(what.getBounds().getConstantValue() instanceof StringVector) {
        StringVector whatClasses = (StringVector) what.getBounds().getConstantValue();
        if(!mightInherit(x.getBounds(), whatClasses)) {
          return new ConstantCall(LogicalVector.FALSE);
        }
      }
    }

    return new StaticMethodCall(fallback, arguments, ValueBounds.builder()
        .setTypeSet(TypeSet.INT | TypeSet.LOGICAL)
        .build());
  }

  private boolean mightInherit(ValueBounds x, StringVector whatClasses) {
    for (String whatClass : whatClasses) {
      if(mightInherit(x, whatClass)) {
        return true;
      }
    }
    return false;
  }

  private boolean mightInherit(ValueBounds x, String className) {
    if(className.equals("numeric")) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.DOUBLE);

    } else if(className.equals(LogicalVector.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.LOGICAL);

    } else if(className.equals(ComplexVector.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.COMPLEX);

    } else if(className.equals(RawVector.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.RAW);

    } else if(className.equals(IntVector.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.INT);

    } else if(className.equals(StringVector.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.STRING);

    } else if(className.equals("matrix") || className.equals("array")) {
      return x.isAnyFlagSet(ValueBounds.MAYBE_DIM);

    } else if(className.equals(Environment.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.ENVIRONMENT);

    } else if(className.equals(Symbol.IMPLICIT_CLASS)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.SYMBOL);

    } else if(className.equals(ListVector.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.LIST);

    } else if(className.equals(PairList.Node.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.PAIRLIST);

    } else if(className.equals(FunctionCall.IMPLICIT_CLASS)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.CALL);

    } else if(className.equals(Function.IMPLICIT_CLASS)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.FUNCTION);

    } else if(className.equals(ExpressionVector.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.EXPRESSION);

    } else if(className.equals(ExternalPtr.TYPE_NAME)) {
      return TypeSet.mightBe(x.getTypeSet(), TypeSet.EXTERNAL_PTR);

    } else {
      // Phew. If not an implicit class name, then check class attribute
      if(!x.isFlagSet(ValueBounds.MAYBE_CLASS)) {
        return false;
      }
      if(x.getConstantClassAttribute() instanceof StringVector) {
        StringVector knownClasses = (StringVector) x.getConstantClassAttribute();
        for (String knownClass : knownClasses) {
          if(knownClass.equals(className)) {
            return true;
          }
        }
        return false;
      }

      return true;
    }
  }
}
