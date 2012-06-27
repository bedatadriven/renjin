package org.renjin.jvminterop.converters;

import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;


public class IntegerArrayConverter implements Converter<Number[]> {

  public static final IntegerArrayConverter INSTANCE = new IntegerArrayConverter();

  private IntegerArrayConverter() {
  }

  @Override
  public SEXP convertToR(Number[] value) {
    if (value == null) {
      return new IntArrayVector(IntVector.NA);
    } else {
      int iArray[] = new int[value.length];
      for (int i = 0; i < value.length; i++) {
        iArray[i] = value[i].intValue();
      }
      return new IntArrayVector(iArray);
    }
  }

  public static boolean accept(Class clazz) {
    Class iclazz = clazz.getComponentType();
    return clazz.isArray()
        && (iclazz == Integer.TYPE || iclazz == Integer.class
            || iclazz == Short.TYPE || iclazz == Short.class);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return  exp instanceof IntVector;
//        || exp instanceof LogicalVector;exp instanceof DoubleVector ||
  }

  @Override
  public int getSpecificity() {
    return Specificity.INTEGER;
  }

  @Override
  public Object convertToJava(SEXP value) {  
    if(!(value instanceof AtomicVector)) {
      throw new EvalException("It's not an AtomicVector", value.getTypeName());
    } else if(value.length() < 1) {
      //to keep its type info
      return new Integer[0];
    }
    IntVector lv= (IntVector)value;
    int length = lv.length();
    Integer[] values = new Integer[length];
    for(int i=0;i<length;i++){
      values[i]= lv.getElementAsObject(i);
    }
    return values;
  }
}
