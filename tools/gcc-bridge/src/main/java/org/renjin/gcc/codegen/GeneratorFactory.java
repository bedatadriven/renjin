package org.renjin.gcc.codegen;

import org.renjin.gcc.codegen.param.*;
import org.renjin.gcc.codegen.ret.PrimitiveReturnGenerator;
import org.renjin.gcc.codegen.ret.PtrReturnGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.ret.VoidReturnGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constructs a set of parameter generators for a list of {@code GimpleParameter}s
 */
public class GeneratorFactory {


  public ParamGenerator forParameter(GimpleType parameterType) {
    if(parameterType instanceof GimplePrimitiveType) {
      return new PrimitiveParamGenerator(parameterType);
      
    } else if(parameterType instanceof GimpleIndirectType) {
      // pointer to a pointer?
      GimpleIndirectType pointerType = (GimpleIndirectType) parameterType;
      
      if(pointerType.getBaseType() instanceof GimpleFunctionType) {
        return new FunPtrParamGenerator(parameterType);
        
      } else if(pointerType.getBaseType() instanceof GimpleIndirectType) {
        return new WrappedPtrPtrParamGenerator(parameterType);
      
      } else {
        return new WrappedPtrParamGenerator(parameterType);
      }
    }
    
    throw new UnsupportedOperationException("Parameter type: " + parameterType);
  }

  public ReturnGenerator findReturnGenerator(GimpleType returnType) {
    if(returnType instanceof GimpleVoidType) {
      return new VoidReturnGenerator();
      
    } else if(returnType instanceof GimplePrimitiveType) {
      return new PrimitiveReturnGenerator(returnType);
    
    } else if(returnType instanceof GimpleIndirectType) {
      return new PtrReturnGenerator(returnType);
    
    } else {
      throw new UnsupportedOperationException("Return type: " + returnType);
    }
  }

  public List<ParamGenerator> forParameterTypes(List<GimpleType> parameterTypes) {
    List<ParamGenerator> generators = new ArrayList<ParamGenerator>();
    for (GimpleType parameterType : parameterTypes) {
      ParamGenerator param = forParameter(parameterType);
      generators.add(param);
    }
    return generators;
  }

  public Map<GimpleParameter, ParamGenerator> forParameters(List<GimpleParameter> parameters) {
    Map<GimpleParameter, ParamGenerator> map = new HashMap<GimpleParameter, ParamGenerator>();
    for (GimpleParameter parameter : parameters) {
      map.put(parameter, forParameter(parameter.getType()));
    }
    return map;
  }
}
