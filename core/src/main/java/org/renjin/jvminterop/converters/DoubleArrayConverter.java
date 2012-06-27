package org.renjin.jvminterop.converters;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.lang.reflect.Array;


public class DoubleArrayConverter implements Converter<Object> {

  public final Class componentClass;

  public DoubleArrayConverter(Class clazz) {
    componentClass = clazz.getComponentType();
  }

  public static boolean accept(Class clazz) {
    Class iclazz = clazz.getComponentType();
    return clazz.isArray()
        && (iclazz == Double.TYPE || iclazz == Double.class
            || iclazz == Float.TYPE || iclazz == Float.class
            || iclazz == Long.TYPE || iclazz == Long.class);
  }

  @Override
  public SEXP convertToR(Object value) {
    if (value == null) {
      return new DoubleArrayVector(DoubleVector.NA);
    } else {
      double dArray[] = new double[Array.getLength(value)];
      for (int i = 0; i < Array.getLength(value); i++) {
        dArray[i] = ((Number)Array.get(value, i)).doubleValue();
      }
      return new DoubleArrayVector(dArray);
    }
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return  exp instanceof DoubleVector ||
            exp instanceof IntVector ||
            exp instanceof LogicalVector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.DOUBLE;
  }

  @Override
  public Object convertToJava(SEXP value) {  
    if(!(value instanceof AtomicVector)) {
      throw new EvalException("It's not an AtomicVector", value.getTypeName());
    } else if(value.length() < 1) {
      //to keep its type info
      return new Double[0];
    }
    AtomicVector dv= (AtomicVector)value;
    int length = dv.length();
   
    Object array = Array.newInstance(componentClass, value.length());
    for(int i=0;i<length;i++){
      Array.set(array, i, dv.getElementAsObject(i));
    }
    return array;
  }
}
