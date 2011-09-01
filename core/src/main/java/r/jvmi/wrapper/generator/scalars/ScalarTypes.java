package r.jvmi.wrapper.generator.scalars;

import java.util.Map;

import r.jvmi.wrapper.GeneratorDefinitionException;
import r.lang.SEXP;

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
