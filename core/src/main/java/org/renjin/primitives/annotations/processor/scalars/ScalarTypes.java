package org.renjin.primitives.annotations.processor.scalars;

import java.util.Map;

import org.apache.commons.math.complex.Complex;
import org.renjin.primitives.annotations.processor.GeneratorDefinitionException;
import org.renjin.sexp.Logical;
import org.renjin.sexp.SEXP;


import com.google.common.collect.Maps;

public class ScalarTypes {

  private static final ScalarTypes INSTANCE = new ScalarTypes();
  
  private Map<Class, ScalarType> types = Maps.newHashMap();
  private SexpType sexpType = new SexpType();
  
  private ScalarTypes() {
    
    types.put(Integer.TYPE, new IntegerType());
    types.put(String.class, new StringType());
    types.put(Boolean.TYPE, new BooleanType());
    types.put(Double.TYPE, new DoubleType()); 
    types.put(Float.TYPE, new FloatType()); 
    types.put(Logical.class, new LogicalType());
    types.put(Complex.class, new ComplexType());
  }
  
  public static boolean has(Class clazz) {
    return INSTANCE.types.containsKey(clazz);
  }
  
  public static ScalarType get(Class clazz) {
    if(SEXP.class.isAssignableFrom(clazz)) {
      return INSTANCE.sexpType;
    }
    ScalarType type = INSTANCE.types.get(clazz);
    if(type == null) {
      throw new GeneratorDefinitionException(clazz.getName() + " cannot be recycled upon");
    }
    return type;
  }
}
