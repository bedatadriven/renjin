/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.annotations.Struct;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.fatptr.*;
import org.renjin.gcc.codegen.type.complex.ComplexTypeStrategy;
import org.renjin.gcc.codegen.type.fun.FunPtrStrategy;
import org.renjin.gcc.codegen.type.fun.FunTypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveParamStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValueFunction;
import org.renjin.gcc.codegen.type.primitive.StringParamStrategy;
import org.renjin.gcc.codegen.type.record.RecordArrayReturnStrategy;
import org.renjin.gcc.codegen.type.record.RecordArrayValueFunction;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtrStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidReturnStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidTypeStrategy;
import org.renjin.gcc.codegen.vptr.VPtrParamStrategy;
import org.renjin.gcc.codegen.vptr.VPtrReturnStrategy;
import org.renjin.gcc.codegen.vptr.VPtrStrategy;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Provides the {@link TypeStrategy} for each {@link GimpleType}
 * 
 * <p>There is a single instance of the {@code TypeOracle} for each compilation, which
 * might compile several compilation units simultaneously. The {@code TypeOracle} holds all information about 
 * Gimple types at compile time, and so can provide the right strategy for code generation.</p>
 */
public class TypeOracle {

  private final Map<String, RecordTypeStrategy> recordTypes = Maps.newHashMap();

  /**
   * Map from internal JVM class name to a GimpleType
   */
  private final Map<String, GimpleRecordType> classTypes = Maps.newHashMap();

  public void addRecordType(GimpleRecordTypeDef type, RecordTypeStrategy strategy) {
    recordTypes.put(type.getId(), strategy);
    if(strategy instanceof RecordClassTypeStrategy) {
      classTypes.put(((RecordClassTypeStrategy) strategy).getJvmType().getInternalName(), strategy.getRecordType());
    }
  }

  public Collection<RecordTypeStrategy> getRecordTypes() {
    return recordTypes.values();
  }

  public PointerTypeStrategy forPointerType(GimpleType type) {
    if(!(type instanceof GimpleIndirectType)) {
      throw new IllegalArgumentException("not a pointer type: " + type);
    }
    
    return forType(type.getBaseType()).pointerTo();
  }

  public ArrayTypeStrategy forArrayType(GimpleType type) {
    if(!(type instanceof GimpleArrayType)) {
      throw new IllegalArgumentException("not an array type: " + type);
    }
    return (ArrayTypeStrategy) forType(type);

  }
  
  public TypeStrategy forType(GimpleType type) {
    if(type instanceof GimplePrimitiveType) {
      return new PrimitiveTypeStrategy((GimplePrimitiveType) type);

    } else if(type instanceof GimpleComplexType) {
      return new ComplexTypeStrategy((GimpleComplexType) type);
    
    } else if(type instanceof GimpleFunctionType) {
      return new FunTypeStrategy((GimpleFunctionType) type);

    } else if(type instanceof GimpleVoidType) {
      return new VoidTypeStrategy();
      
    } else if (type instanceof GimpleRecordType) {
      GimpleRecordType recordType = (GimpleRecordType) type;
      return forRecordType(recordType);
      
    } else if(type instanceof GimpleIndirectType) {
      return forType(type.getBaseType()).pointerTo();

    } else if(type instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) type;
      return forType(arrayType.getComponentType()).arrayOf(arrayType);
    
    } else {
      throw new UnsupportedOperationException("Unsupported type: " + type);
    }
  }


  private RecordTypeStrategy forRecordType(GimpleRecordType recordType) {
    RecordTypeStrategy recordTypeStrategy = recordTypes.get(recordType.getId());
    if(recordTypeStrategy == null) {
      throw new InternalCompilerException(String.format(
          "No record type for GimpleRecordType[name: %s, id: %s]", recordType.getName(), recordType.getId()));
    }
    return recordTypeStrategy;
  }
  

  public ParamStrategy forParameter(GimpleType parameterType) {
    return forType(parameterType).getParamStrategy();
  }

  /**
   * Creates a new FieldGenerator for a given field type.
   * 
   * @param className the full internal name of the class in which the field is declared (for example, "org/renjin/gcc/Struct")
   * @param field the gimple field
   */
  public FieldStrategy forField(Type className, GimpleField field) {
    
    String fieldName = field.getName();
    if(Strings.isNullOrEmpty(fieldName)) {
      fieldName = "$$field" + field.getOffset();
    }
    fieldName = fieldName.replace('.', '$');
    
    TypeStrategy type = forType(field.getType());
    if(field.isAddressed()) {
      return type.addressableFieldGenerator(className, fieldName);
    } else {
      return type.fieldGenerator(className, fieldName);
    }
  }

  public ReturnStrategy returnStrategyFor(GimpleType returnType) {
    return forType(returnType).getReturnStrategy();
  }
  
  public ReturnStrategy forReturnValue(Method method) {
    Class<?> returnType = method.getReturnType();
    if(returnType.equals(void.class)) {
      return new VoidReturnStrategy();

    } else if(returnType.isPrimitive()) {
      return new SimpleReturnStrategy(new PrimitiveTypeStrategy(returnType));

    } else if(Ptr.class.isAssignableFrom(returnType)) {
      return new VPtrReturnStrategy();

    } else if(classTypes.containsKey(Type.getInternalName(returnType))) {
      GimpleRecordType recordType = classTypes.get(Type.getInternalName(returnType));
      return recordTypes.get(recordType.getId()).pointerTo().getReturnStrategy();

    } else if(returnType.equals(Object.class)) {
      return new SimpleReturnStrategy(new VoidPtrStrategy());

    } else if(method.isAnnotationPresent(Struct.class)) {
      Type arrayType = Type.getReturnType(method);
      Type componentType = Type.getType(arrayType.getDescriptor().substring(1));
      return new RecordArrayReturnStrategy(new RecordArrayValueFunction(componentType, 0, null), arrayType, 0);

    } else if(returnType.equals(MethodHandle.class)) {
      return new FunPtrStrategy().getReturnStrategy();
      
    } else {
      throw new UnsupportedOperationException(String.format(
          "Unsupported return type %s in method %s.%s",
          returnType.getName(),
          method.getDeclaringClass().getName(), method.getName()));
    }
  }

  /**
   * Creates a list of {@code ParamGenerators} from an existing JVM method.
   *
   * <p>Note that there is not a one-to-one relationship between JVM method parameters and
   * our {@code ParamGenerators}; a complex pointer is represented as a {@code double[]} and an 
   * {@code int} offset, for example.</p>
   */
  public List<ParamStrategy> forParameterTypesOf(Method method) {

    List<ParamStrategy> strategies = new ArrayList<>();

    int numParams;
    if(method.isVarArgs()) {
      numParams = method.getParameterTypes().length - 1;
    } else {
      numParams = method.getParameterTypes().length;
    }
    
    int index = 0;
    while(index < numParams) {
      Class<?> paramClass = method.getParameterTypes()[index];
      if(paramClass.equals(ObjectPtr.class)) {
        strategies.add(forObjectPtrParam(method.getGenericParameterTypes()[index]));
        index++;
        
      } else if (Ptr.class.isAssignableFrom(paramClass)) {
        strategies.add(new VPtrParamStrategy());
        index++;

      } else if (paramClass.isPrimitive()) {
        strategies.add(new PrimitiveParamStrategy(GimplePrimitiveType.fromJvmType(Type.getType(paramClass))));
        index++;

      } else if (paramClass.equals(String.class)) {
        strategies.add(new StringParamStrategy());
        index++;

      } else if (classTypes.containsKey(Type.getInternalName(paramClass))) {
        GimpleRecordType mappedType = classTypes.get(Type.getInternalName(paramClass));
        strategies.add(((RecordClassTypeStrategy) forRecordType(mappedType)).pointerToUnit().getParamStrategy());
        index++;

      } else if (paramClass.equals(MethodHandle.class)) {
        strategies.add(new FunPtrStrategy().getParamStrategy());
        index++;
        
      } else if(paramClass.equals(Object.class)) {
        strategies.add(new VoidPtrStrategy().getParamStrategy());
        index++;
        
      } else {
        throw new UnsupportedOperationException(String.format(
            "Unsupported parameter %d of type %s", 
            index,
            paramClass.getName()));
      } 
    }
    return strategies;
  }


  private Class objectPtrBaseType(java.lang.reflect.Type type) {
    if (!(type instanceof ParameterizedType)) {
      throw new InternalCompilerException(ObjectPtr.class.getSimpleName() + " parameters must be parameterized");
    }
    ParameterizedType parameterizedType = (ParameterizedType) type;
    return (Class) parameterizedType.getActualTypeArguments()[0];
  }
  
  private ParamStrategy forObjectPtrParam(java.lang.reflect.Type type) {
    Class baseType = objectPtrBaseType(type);
    if(baseType.equals(BytePtr.class)) {
      return forType(new GimpleIntegerType(8)).pointerTo().pointerTo().getParamStrategy();
    } else {
      String baseTypeInternalName = Type.getInternalName((Class)baseType);
      if(classTypes.containsKey(baseTypeInternalName)) {
        GimpleRecordType mappedType = classTypes.get(baseTypeInternalName);
        return recordTypes.get(mappedType.getId()).pointerTo().getParamStrategy();
      }
    }
    throw new UnsupportedOperationException("TODO: baseType = " + baseType);
  }

  public Map<GimpleParameter, ParamStrategy> forParameters(List<GimpleParameter> parameters) {
    Map<GimpleParameter, ParamStrategy> map = new HashMap<GimpleParameter, ParamStrategy>();
    for (GimpleParameter parameter : parameters) {
      try {
        map.put(parameter, forParameter(parameter.getType()));
      } catch(Exception e) {
        throw new InternalCompilerException(String.format(
            "Exception for parameter %s of type %s", parameter.getName(), parameter.getType()), e);
      }
    }
    return map;
  }
  
  public static String getMethodDescriptor(ReturnStrategy returnStrategy, List<ParamStrategy> paramStrategies) {
    List<Type> types = Lists.newArrayList();
    for (ParamStrategy paramStrategy : paramStrategies) {
      types.addAll(paramStrategy.getParameterTypes());
    }

    Type[] typesArray = types.toArray(new Type[types.size()]);
    
    return Type.getMethodDescriptor(returnStrategy.getType(), typesArray);
  }

}
