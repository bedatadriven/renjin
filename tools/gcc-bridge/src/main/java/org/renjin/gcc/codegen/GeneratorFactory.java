package org.renjin.gcc.codegen;

import com.google.common.collect.Maps;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.PrimitiveParamGenerator;
import org.renjin.gcc.codegen.param.PrimitivePtrParamGenerator;
import org.renjin.gcc.codegen.param.StringParamGenerator;
import org.renjin.gcc.codegen.ret.*;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.ObjectPtr;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constructs a set of parameter generators for a list of {@code GimpleParameter}s
 */
public class GeneratorFactory {

  private final Map<String, RecordClassGenerator> recordTypes = Maps.newHashMap();

  /**
   * Map from internal JVM class name to a GimpleType
   */
  private final Map<String, GimpleRecordType> classTypes = Maps.newHashMap();
  
  public void addRecordType(GimpleRecordTypeDef type, RecordClassGenerator generator) {
    recordTypes.put(type.getId(), generator);
    classTypes.put(generator.getClassName(), generator.getGimpleType());
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

  public ReturnGenerator forReturnValue(Method method) {
    Class<?> returnType = method.getReturnType();
    if(returnType.equals(void.class)) {
      return new VoidReturnGenerator();

    } else if(returnType.isPrimitive()) {
      return new PrimitiveReturnGenerator(GimplePrimitiveType.fromJvmType(returnType));

    } else if(WrapperType.is(returnType)) {
      WrapperType wrapperType = WrapperType.valueOf(returnType);
      return new PrimitivePtrReturnGenerator(wrapperType.getGimpleType());

    } else if(classTypes.containsKey(Type.getInternalName(returnType))) {
      GimpleRecordType recordType = classTypes.get(Type.getInternalName(returnType));
      return new RecordPtrReturnGenerator(recordTypes.get(recordType.getId()));

    } else {
      throw new UnsupportedOperationException(String.format(
          "Unsupported return type %s in method %s.%s",
          returnType.getName(),
          method.getDeclaringClass().getName(), method.getName()));
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


  /**
   * Creates a list of {@code ParamGenerators} from an existing JVM method.
   *
   * <p>Note that there is not a one-to-one relationship between JVM method parameters and
   * our {@code ParamGenerators}; a complex pointer is represented as a {@code double[]} and an 
   * {@code int} offset, for example.</p>
   */
  public List<ParamGenerator> forParameterTypesOf(Method method) {

    List<ParamGenerator> generators = new ArrayList<ParamGenerator>();

    int numParams = method.getParameterTypes().length;
    
    int index = 0;
    while(index < numParams) {
      Class<?> paramClass = method.getParameterTypes()[index];
      if(paramClass.equals(ObjectPtr.class)) {
        generators.add(forPointerPointerParameter(method.getGenericParameterTypes()[index]));
        index++;
        
      } else if (WrapperType.is(paramClass) && !paramClass.equals(CharPtr.class)) {
        WrapperType wrapperType = WrapperType.valueOf(paramClass);
        generators.add(new PrimitivePtrParamGenerator(wrapperType.getGimpleType()));
        index++;

      } else if (paramClass.isPrimitive()) {
        generators.add(new PrimitiveParamGenerator(GimplePrimitiveType.fromJvmType(paramClass)));
        index++;

      } else if (paramClass.equals(String.class)) {
        generators.add(new StringParamGenerator());
        index++;

      } else if (classTypes.containsKey(Type.getInternalName(paramClass))) {
        GimpleRecordType mappedType = classTypes.get(Type.getInternalName(paramClass));
        generators.add(forType(mappedType).pointerTo().paramGenerator());
        index++;
        
      } else {
        throw new UnsupportedOperationException(String.format(
            "Unsupported parameter %d of type %s", 
            index,
            paramClass.getName()));
      } 
    }
    return generators;
  }
  
  private ParamGenerator forPointerPointerParameter(java.lang.reflect.Type type) {
    if(!(type instanceof ParameterizedType)) {
      throw new InternalCompilerException(ObjectPtr.class.getSimpleName() + " parameters must be parameterized");
    }
    ParameterizedType parameterizedType = (ParameterizedType) type;
    java.lang.reflect.Type baseType = parameterizedType.getActualTypeArguments()[0];
    
    if(baseType.equals(BytePtr.class)) {
      return forType(new GimpleIntegerType(8)).pointerTo().pointerTo().paramGenerator();
    } else {
      throw new UnsupportedOperationException("TODO: baseType = " + baseType);
    }
  }

  public Map<GimpleParameter, ParamGenerator> forParameters(List<GimpleParameter> parameters) {
    Map<GimpleParameter, ParamGenerator> map = new HashMap<GimpleParameter, ParamGenerator>();
    for (GimpleParameter parameter : parameters) {
      map.put(parameter, forParameter(parameter.getType()));
    }
    return map;
  }
}
