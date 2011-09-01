package r.jvmi.wrapper.generator.scalars;

import java.util.Map;

import com.google.common.collect.Maps;

public class ScalarTypes {

  private static final ScalarTypes INSTANCE = new ScalarTypes();
  
  private Map<Class, ScalarType> types = Maps.newHashMap();
  
  private ScalarTypes() {
    
    types.put(Integer.TYPE, new IntegerType());
    types.put(String.class, new StringType());
    types.put(Boolean.TYPE, new BooleanType());
    types.put(Double.TYPE, new DoubleType());  
  }
  
  public static ScalarType get(Class clazz) {
    ScalarType type = INSTANCE.types.get(clazz);
    if(type == null) {
      throw new IllegalArgumentException(clazz.getName());
    }
    return type;
  }
}
