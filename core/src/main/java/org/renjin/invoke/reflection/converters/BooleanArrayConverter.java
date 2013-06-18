package org.renjin.invoke.reflection.converters;

import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;


/**
 * Converts between {@code boolean[]} and R {@code logical} vectors
 */
public class BooleanArrayConverter implements Converter<Boolean[]> {

  public static final BooleanArrayConverter INSTANCE = new BooleanArrayConverter();

  public static boolean accept(Class clazz) {
    return clazz.isArray() &&( clazz.getComponentType() == Boolean.class||clazz.getComponentType()== Boolean.TYPE);
  }
  
  @Override
  public LogicalVector convertToR(Boolean[] value) {
    if(value == null) {
      return new LogicalArrayVector(LogicalVector.NA);
    } else {
      return new LogicalArrayVector(value);
    }
  }
  
  @Override
  public Object convertToJava(SEXP value) {  
    if(!(value instanceof AtomicVector)) {
      throw new EvalException("It's not an AtomicVector", value.getTypeName());
    } else if(value.length() < 1) {
      //to keep its type info
      return new Boolean[0];
    }
    LogicalVector lv= (LogicalVector)value;
    int length = lv.length();
    Boolean[] values = new Boolean[length];
    for(int i=0;i<length;i++){
      values[i]= lv.getElementAsObject(i);
    }
    return values;
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof LogicalVector && exp.length() >= 1;
  }

  @Override
  public int getSpecificity() {
    return Specificity.SPECIFIC_OBJECT;
  }
}
