package org.renjin.gcc.codegen;

import com.google.common.collect.Maps;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.type.*;
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

  private final Map<String, RecordClassGenerator> recordTypes = Maps.newHashMap();
  
  public void addRecordType(GimpleRecordTypeDef type, RecordClassGenerator generator) {
    recordTypes.put(type.getId(), generator);
  }
  
  
  public TypeFactory forType(GimpleType type) {
    if(type instanceof GimplePrimitiveType) {
      return new PrimitiveTypeFactory((GimplePrimitiveType) type);

    } else if(type instanceof GimpleComplexType) {
      return new ComplexTypeFactory((GimpleComplexType) type);
    
    } else if(type instanceof GimpleFunctionType) {
      return new FunTypeFactory((GimpleFunctionType) type);

    } else if(type instanceof GimpleVoidType) {
      return new VoidTypeFactory();
      
    } else if (type instanceof GimpleRecordType) {
      GimpleRecordType recordType = (GimpleRecordType) type;
      RecordClassGenerator recordGenerator = recordTypes.get(recordType.getId());
      if(recordGenerator == null) {
        throw new InternalCompilerException(String.format(
            "No record type for GimpleRecordType[name: %s, id: %s]", recordType.getName(), recordType.getId()));
      }
      return new RecordTypeFactory(recordGenerator);
      
    } else if(type instanceof GimpleIndirectType) {
      return forType(type.getBaseType()).pointerTo();
    
    } else if(type instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) type;
      return forType(arrayType.getComponentType()).arrayOf(arrayType);
    
    } else {
      throw new UnsupportedOperationException("Unsupported type: " + type);
    }
  }

  public ParamGenerator forParameter(GimpleType parameterType) {
    return forType(parameterType).paramGenerator();
  }

  /**
   * Creates a new FieldGenerator for a given field type.
   * 
   * @param className the full internal name of the class in which the field is declared (for example, "org/renjin/gcc/Struct")
   * @param fieldName the name of the field
   * @param type the GimpleType of the field
   */
  public FieldGenerator forField(String className, String fieldName, GimpleType type) {
    return forType(type).fieldGenerator(className, fieldName); 
  }
  
  public ReturnGenerator findReturnGenerator(GimpleType returnType) {
    return forType(returnType).returnGenerator();
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
