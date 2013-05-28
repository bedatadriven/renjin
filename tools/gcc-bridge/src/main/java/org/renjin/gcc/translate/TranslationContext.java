package org.renjin.gcc.translate;

import java.lang.reflect.Field;
import java.util.List;

import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleOutput;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.SyntheticJimpleType;
import org.renjin.gcc.translate.call.GccFunction;
import org.renjin.gcc.translate.call.MethodRef;
import org.renjin.gcc.translate.type.struct.ImRecordType;
import org.renjin.gcc.translate.type.*;

import com.google.common.collect.Lists;

public class TranslationContext {
  private JimpleClassBuilder mainClass;
  private MethodTable methodTable;
  private List<GimpleFunction> functions;
  private FunPtrTable funPtrTable;
  private RecordTypeTable recordTypeTable;

  public TranslationContext(JimpleClassBuilder mainClass, MethodTable methodTable, List<GimpleFunction> functions) {
    this.mainClass = mainClass;
    this.methodTable = methodTable;
    this.functions = functions;
    this.funPtrTable = new FunPtrTable(this);
    this.recordTypeTable = new RecordTypeTable(this);
  }

  public JimpleClassBuilder getMainClass() {
    return mainClass;
  }

  public MethodRef resolveMethod(String name) {
    MethodRef ref = resolveInternally(name);
    if (ref != null) {
      return ref;
    }
    return methodTable.resolve(name);
  }

  public MethodRef resolveMethod(GimpleCall call, CallingConvention callingConvention) {

    return resolveMethod(callingConvention.mangleFunctionName(functionName(call)));
  }

  private String functionName(GimpleCall call) {
    if (call.getFunction() instanceof GimpleAddressOf) {
      
      GimpleExpr functionValue = ((GimpleAddressOf) call.getFunction()).getValue();
      if(functionValue instanceof GimpleFunctionRef) {
        return ((GimpleFunctionRef) functionValue).getName();
      } 
    } 
    throw new UnsupportedOperationException(call.toString());
  }

  private MethodRef resolveInternally(String name) {
    for (GimpleFunction function : functions) {
      if (function.getMangledName().equals(name)) {
        return asRef(function);
      }
    }
    return null;
  }

  private MethodRef asRef(GimpleFunction function) {
    
    JimpleType returnType;
    if(function.getReturnType() instanceof GimpleVoidType) {
      returnType = JimpleType.VOID;
    } else {
      returnType = resolveType(function.getReturnType()).returnType();
    }
    List<JimpleType> paramTypes = Lists.newArrayList();
    for (GimpleParameter param : function.getParameters()) {
      paramTypes.add(resolveType(param.getType()).paramType());
    }
    return new GccFunction(mainClass.getFqcn(), function.getName(), returnType, paramTypes);
  }

  public Field findGlobal(String name) {
    return methodTable.findGlobal(name);
  }

  public ImType resolveType(GimpleType type) {
    if (type instanceof GimplePrimitiveType) {
      return ImPrimitiveType.valueOf(type);

    } else if (type instanceof GimpleRecordType) {
      return resolveRecordType((GimpleRecordType) type);

    } else if(type instanceof GimpleFunctionType) {
      return funPtrTable.resolveFunctionType((GimpleFunctionType) type);

    } else if (type instanceof GimpleIndirectType) {
      GimpleType baseType = type.getBaseType();

      // treat pointers to an array as simply pointers to the underlying type
      if(baseType instanceof GimpleArrayType) {
        baseType = ((GimpleArrayType) baseType).getComponentType();
      }
      return resolveType(baseType).pointerType();
    }
    throw new UnsupportedOperationException(type.toString());
  }

  public JimpleOutput getJimpleOutput() {
    return mainClass.getOutput();
  }

  public String getFunctionPointerInterfaceName(GimpleFunctionType type) {
    return funPtrTable.getInterfaceName(type);
  }

  public ImFunctionType getFunctionPointerMethod(GimpleFunctionType type) {
    return funPtrTable.methodRef(type);
  }

  public String getInvokerClass(MethodRef method) {
    return funPtrTable.getInvokerClassName(method);
  }

  public List<GimpleFunction> getFunctions() {
    return functions;
  }

  public ImRecordType resolveRecordType(GimpleRecordType recordType) {
    return recordTypeTable.resolveStruct(recordType);
  }

  public JimpleType getInvokerType(MethodRef method) {
    return new SyntheticJimpleType(getInvokerClass(method));
  }

}
