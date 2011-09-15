package r.jvmi.wrapper.generator.args;

import java.util.Map;

import r.jvmi.binding.JvmMethod.Argument;

import com.google.common.collect.Maps;

public class ToScalar extends ArgConverterStrategy {

  private Map<Class, String> methods = Maps.newHashMap();
  
  public ToScalar() {
    methods.put(Integer.class, "convertToInteger");
    methods.put(Integer.TYPE, "convertToInt");
    methods.put(String.class, "convertToString");
    methods.put(Boolean.TYPE, "convertToBooleanPrimitive");
    methods.put(Double.TYPE, "convertToDoublePrimitive");
  }
  
  
  @Override
  public boolean accept(Argument formal) {
    return methods.containsKey(formal.getClazz());
  }

  @Override
  public String conversionExpression(Argument formal, String argumentExpression) {
    return methods.get(formal.getClazz()) + "(" + argumentExpression + ")";
  }

}
