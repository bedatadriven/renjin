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
package org.renjin.primitives.ni;


import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.BooleanPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Vector;

import java.lang.invoke.MethodHandle;

/**
 * A call to a compiled native method through either .C or .Fortran that
 * is known to be without side affects and has been deferred.
 */
public class DeferredFortranCall implements DeferredNativeCall {

  private String methodName;
  private final MethodHandle method;
  private final Class<?>[] parameterType;
  
  private final Vector[] operands;
  private final Object outputArrays[];
  private final ListVector outputList;
  
  private boolean inputsDeferred = false;
  
  private boolean evaluated = false;
  
  
  public DeferredFortranCall(String methodName, MethodHandle method, ListVector inputs) {
    this.methodName = methodName;
    this.method = method;

    parameterType = method.type().parameterArray();
    if(parameterType.length != inputs.length()) {
      throw new EvalException("Invalid number of args");
    }

    operands = new Vector[inputs.length()];
    outputArrays = new Object[inputs.length()];
    
    // Construct a named list with placeholders for the output

    ListVector.NamedBuilder outputList = new ListVector.NamedBuilder();
    for(int i = 0; i!= inputs.length(); ++i) {
      AtomicVector vector = (AtomicVector) inputs.get(i);
      
      operands[i] = vector;
      
      if(vector.isDeferred()) {
        inputsDeferred = true;
      }
      
      if(parameterType[i].equals(DoublePtr.class)) {
        outputList.add(inputs.getName(i), new NativeOutputDoubleVector(this, i, vector.length(), vector.getAttributes()));
      
      } else if(parameterType[i].equals(IntPtr.class)) {
        outputList.add(inputs.getName(i), new NativeOutputIntVector(this, i, vector.length(), vector.getAttributes()));

      } else if(parameterType[i].equals(BooleanPtr.class)) {
        outputList.add(inputs.getName(i), new NativeOutputBoolVector(this, i, vector.length(), vector.getAttributes()));
        
      } else {
        throw new UnsupportedOperationException("fortran type: " + parameterType[i]);
      }
    }
    this.outputList = outputList.build();
  }

  @Override
  public Vector[] getOperands() {
    return operands;
  }

  @Override
  public String getOutputName(int outputIndex) {
    return outputList.getNames().getElementAsString(outputIndex);
  }

  public boolean isEvaluated() {
    return evaluated;
  }

  @Override
  public Object output(int outputIndex) {
    if(!evaluated) {
      throw new IllegalStateException("Not evaluated yet.");
    }
    return outputArrays[outputIndex];
  }

  @Override
  public String getDebugName() {
    return methodName;
  }

  public ListVector getOutputList() {
    return outputList;
  }

  @Override
  public void evaluate(Vector[] operands) {
    
    // First make copies of the input arguments
    for (int i = 0; i < operands.length; i++) {
      AtomicVector inputVector = (AtomicVector) operands[i];
      if(parameterType[i].equals(DoublePtr.class)) {
        outputArrays[i] = inputVector.toDoubleArray();
      } else if(parameterType[i].equals(IntPtr.class)) {
        outputArrays[i] = inputVector.toIntArray();
      } else if(parameterType[i].equals(BooleanPtr.class)) {
        outputArrays[i] = toBooleanArray(inputVector);
      } else {
        throw new UnsupportedOperationException("parameterType: " + parameterType);
      }
    }
    invoke();
  }

  private static boolean[] toBooleanArray(AtomicVector vector) {
    boolean array[] = new boolean[vector.length()];
    for(int i=0;i<vector.length();++i) {
      int element = vector.getElementAsRawLogical(i);
      if(element == IntVector.NA) {
        throw new EvalException("NAs cannot be passed to logical fortran argument");
      }
      array[i] = (element != 0);
    }
    return array;
  }

  private void invoke() {
    // Wrap the input arguments into FatPtrs
    Object[] fortranArgs = new Object[operands.length];
    for (int i = 0; i < operands.length; i++) {
      if(parameterType[i].equals(DoublePtr.class)) {
        fortranArgs[i] = new DoublePtr((double[]) outputArrays[i]);
      } else if(parameterType[i].equals(IntPtr.class)) {
        fortranArgs[i] = new IntPtr((int[]) outputArrays[i]);
      } else if(parameterType[i].equals(BooleanPtr.class)) {
        fortranArgs[i] = new BooleanPtr((boolean[])outputArrays[i]);
      } else {
        throw new UnsupportedOperationException("parameterType: " + parameterType[i]);
      }    
    }

    try {
      method.invokeWithArguments(fortranArgs);
      evaluated = true;
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException("Exception thrown while executing " + method, e);
    } 
  }
}
