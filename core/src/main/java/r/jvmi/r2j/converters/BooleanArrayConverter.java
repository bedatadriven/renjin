package r.jvmi.r2j.converters;

import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.Logical;
import r.lang.LogicalVector;
import r.lang.SEXP;
import r.lang.Vector;
import r.lang.exception.EvalException;

public class BooleanArrayConverter implements Converter<Boolean[]> {

  public static final Converter INSTANCE = new BooleanArrayConverter();

  public static boolean accept(Class clazz) {
    return clazz.isArray() &&( clazz.getComponentType() == Boolean.class||clazz.getComponentType()== Boolean.TYPE);
  }
  
  @Override
  public SEXP convertToR(Boolean[] value) {
    if(value == null) {
      return new LogicalVector(LogicalVector.NA);
    } else {
      return new LogicalVector(value);
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
