package r.jvmi.r2j.converters;

import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.LogicalVector;
import r.lang.SEXP;
import r.lang.Vector;
import r.lang.exception.EvalException;

public class DoubleArrayConverter implements Converter<Number[]> {

  public static final Converter INSTANCE = new DoubleArrayConverter();

  private DoubleArrayConverter() {
  }

  public static boolean accept(Class clazz) {
    Class iclazz = clazz.getComponentType();
    return clazz.isArray()
        && (iclazz == Double.TYPE || iclazz == Double.class
            || iclazz == Float.TYPE || iclazz == Float.class
            || iclazz == Long.TYPE || iclazz == Long.class);
  }

  @Override
  public SEXP convertToR(Number[] value) {
    if (value == null) {
      return new DoubleVector(DoubleVector.NA);
    } else {
      double dArray[] = new double[value.length];
      for (int i = 0; i < value.length; i++) {
        dArray[i] = value[i].doubleValue();
      }
      return new DoubleVector(dArray);
    }
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof DoubleVector ;//|| exp instanceof IntVector;
//    || exp instanceof LogicalVector
//        || exp instanceof IntVector;
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
    DoubleVector dv= (DoubleVector)value;
    int length = dv.length();
    Double[] values = new Double[length];
    for(int i=0;i<length;i++){
      values[i]= dv.getElementAsObject(i);
    }
    return values;
  }
}
