package org.renjin.primitives.annotations.processor.args;

import org.renjin.primitives.annotations.processor.GeneratorDefinitionException;
import org.renjin.primitives.annotations.processor.JvmMethod;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;


public class ArgConverterStrategies {

  public static ArgConverterStrategy findArgConverterStrategy(JvmMethod.Argument formal) {
    if(Recyclable.accept(formal)) {
      return new Recyclable(formal);
    
    } else if(UsingAsCharacter.accept(formal)) {
      return new UsingAsCharacter(formal);
    
    } else if(SexpSubclass.accept(formal)) {
      return new SexpSubclass(formal);
    
    } else if(ToScalar.accept(formal)) {
      return new ToScalar(formal);
    
    } else if(UnwrapExternalObject.accept(formal)) {
      return new UnwrapExternalObject(formal);
    }
    
    throw new GeneratorDefinitionException("Could not find a strategy for converting to argument " + formal.getIndex() + " of type " + formal.getClazz());
  }

}
