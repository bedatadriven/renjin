package r.jvmi.r2j.converters;

public class Converters  {

  public static Converter get(Class clazz) {
    if(StringConverter.accept(clazz)) {
      return StringConverter.INSTANCE;
    
    } else if(BooleanConverter.accept(clazz)) {
      return BooleanConverter.INSTANCE;
    
    } else if(IntegerConverter.accept(clazz)) {
      return IntegerConverter.INSTANCE;
      
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
     
    } else {
      return new ObjectConverter(clazz);
    }
  }
  
}
