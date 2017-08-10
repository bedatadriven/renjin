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
package org.renjin.compiler.ir.tac;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.ApplyCallCompiler;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ApplyCallCompilerTest extends EvalTestCase {

  @Test
  public void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

    Closure function = (Closure) eval("function(x) x * 2");
    Vector vector = (Vector)eval("1:1000");

    ApplyCallCompiler compiler = new ApplyCallCompiler(topLevelContext, global, function, vector);
    compiler.tryCompile();

    assertThat(compiler.getFunctionBounds(), equalTo(ValueBounds.primitive(TypeSet.DOUBLE)));
    assertThat(compiler.isPure(), equalTo(true));

    Class<?> compiledClass = compiler.compile();

    // Verify that the static "apply" method is correct
    Method applyMethod = compiledClass.getMethod("apply", int.class);
    Double result = (Double) applyMethod.invoke(null, 8);

    assertThat(result, equalTo(16d));

    // Now verify that we can use this as a DeferredVector
    Constructor<?> constructor = compiledClass.getConstructor(Vector.class, AttributeMap.class);
    DeferredComputation deferredComputation = (DeferredComputation) constructor.newInstance(vector, AttributeMap.EMPTY);

    assertThat(deferredComputation.length(), equalTo(vector.length()));
    assertThat(deferredComputation.getElementAsDouble(0) /* = 1*2 */, equalTo(2d));
    assertThat(deferredComputation.getElementAsDouble(1) /* = 2*2 */, equalTo(4d));
    assertThat(deferredComputation.getElementAsDouble(2) /* = 3*2 */, equalTo(6d));

    Vector[] operands = deferredComputation.getOperands();
    assertThat(operands.length, equalTo(1));
    assertThat(operands[0], is(vector));

  }

}