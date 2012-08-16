package org.renjin.gcc.translate;

import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.GimpleCall;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.expr.GimpleExternal;
import org.renjin.gcc.gimple.struct.Struct;
import org.renjin.gcc.gimple.struct.StructTable;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleMethodRef;
import org.renjin.gcc.jimple.JimpleOutput;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.types.*;

import java.lang.reflect.Field;
import java.util.List;


public class TranslationContext {
  private JimpleClassBuilder mainClass;
  private MethodTable methodTable;
  private List<GimpleFunction> functions;
  private FunPtrTable funPtrTable;
  private StructTable structTable;

  public TranslationContext(JimpleClassBuilder mainClass, MethodTable methodTable, List<GimpleFunction> functions) {
    this.mainClass = mainClass;
    this.methodTable = methodTable;
    this.functions = functions;
    this.funPtrTable = new FunPtrTable(this);
    this.structTable = new StructTable(this);
  }

  public JimpleClassBuilder getMainClass() {
    return mainClass;
  }

  public JimpleMethodRef resolveMethod(String name) {
    JimpleMethodRef ref = resolveInternally(name);
    if(ref != null) {
      return ref;
    }
    return methodTable.resolve(name);
  }


  public JimpleMethodRef resolveMethod(GimpleCall call) {

    String methodName;
    if(call.getFunction() instanceof GimpleExternal) {
      methodName = ((GimpleExternal) call.getFunction()).getName();
    } else {
      throw new UnsupportedOperationException(call.toString());
    }
    return resolveMethod(methodName);
  }

  private JimpleMethodRef resolveInternally(String name) {
    for(GimpleFunction function : functions) {
      if(function.getName().equals(name)) {
        return asRef(function);
      }
    }
    return null;
  }

  private JimpleMethodRef asRef(GimpleFunction function) {
    JimpleType returnType = resolveType(function.returnType()).returnType();
    List<JimpleType> paramTypes = Lists.newArrayList();
    for(GimpleParameter param : function.getParameters()) {
      paramTypes.add(resolveType(param.getType()).paramType());
    }
    return new JimpleMethodRef(mainClass.getFqcn(), function.getName(), returnType, paramTypes);
  }

  public Field findField(GimpleExternal external) {
    return methodTable.findField(external);
  }

  public TypeTranslator resolveType(GimpleType type) {
    if(type instanceof PrimitiveType) {
      return new NumericTypeTranslator((PrimitiveType) type);
    } else if(type instanceof PointerType && ((PointerType) type).getInnerType() instanceof PrimitiveType) {
      return new NumericPtrTypeTranslator((PointerType) type);
    } else if(type instanceof PointerType && ((PointerType) type).getInnerType() instanceof GimpleStructType) {
      return new StructTypeTranslator(this, type);
    } else if(type instanceof FunctionPointerType) {
      return new FunPtrTranslator(this, (FunctionPointerType)type);
    } else if(type instanceof GimpleStructType) {
      return new StructTypeTranslator(this, type);
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  public JimpleOutput getJimpleOutput() {
    return mainClass.getOutput();
  }

  public String getFunctionPointerInterfaceName(FunctionPointerType type) {
    return funPtrTable.getInterfaceName(type);
  }

  public JimpleMethodRef getFunctionPointerMethod(FunctionPointerType type) {
    return funPtrTable.methodRef(type);
  }

  public String getInvokerClass(JimpleMethodRef method) {
    return funPtrTable.getInvokerClassName(method);
  }

  public List<GimpleFunction> getFunctions() {
    return functions;
  }

  public Struct resolveStruct(String name) {
    return structTable.resolveStruct(name);
  }
}
