package org.renjin.jvminterop.converters;

import org.renjin.sexp.SEXP;

public class Converters  {

  public static Converter get(Class clazz) {
    if(StringConverter.accept(clazz)) {
      return StringConverter.INSTANCE;
    
    } else if(BooleanConverter.accept(clazz)) {
      return BooleanConverter.INSTANCE;
    
    } else if(IntegerConverter.accept(clazz)) {
      return IntegerConverter.INSTANCE;
      
    } else if(LongConverter.accept(clazz)) {
      return LongConverter.INSTANCE; 
      
    } else if(DoubleConverter.accept(clazz)) {
      return DoubleConverter.INSTANCE;
    
    } else if(SexpConverter.acceptsJava(clazz)) {
      return new SexpConverter(clazz);
      
    } else if(EnumConverter.accept(clazz)) {
      return new EnumConverter(clazz);
      
    } else if(MapConverter.accept(clazz)) {
      return new MapConverter();
      
    } else if(CollectionConverter.accept(clazz)) {
      return new CollectionConverter();
      
    } else if(StringArrayConverter.accept(clazz)) {
      return StringArrayConverter.INSTANCE;
      
    }else if(BooleanArrayConverter.accept(clazz)) {
      return BooleanArrayConverter.INSTANCE;
      
    } else if(IntegerArrayConverter.accept(clazz)) {
      return IntegerArrayConverter.INSTANCE;
      
    }else if(DoubleArrayConverter.accept(clazz)) {
      return new DoubleArrayConverter(clazz);
      
    }else if(ObjectConverter.accept(clazz)) {
      return ObjectConverter.INSTANCE;
      
    } else {
      return new ObjectOfASpecificClassConverter(clazz);
    }
  }

  public static SEXP fromJava(Object obj) {
    return get(obj.getClass()).convertToR(obj);
  }
}
