package org.renjin.gcc.translate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.jimple.*;
import org.renjin.gcc.translate.call.*;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.field.PrimitiveFieldExpr;
import org.renjin.gcc.translate.type.struct.ImRecordType;
import org.renjin.gcc.translate.type.*;

import com.google.common.collect.Lists;

public class TranslationContext {
  private JimpleClassBuilder mainClass;
  private MethodTable methodTable;
  private List<GimpleFunction> functions = Lists.newArrayList();
  private FunPtrTable funPtrTable;
  private RecordTypeTable recordTypeTable;
  private Map<String, ImExpr> globalVariables = Maps.newHashMap();
  
  private List<CallTranslator> builtinCallTranslators = Lists.newArrayList();

  public TranslationContext(JimpleClassBuilder mainClass, MethodTable methodTable,
                            Map<String, ImRecordType> providedRecordTypes,
                            List<GimpleCompilationUnit> units) {
    this.mainClass = mainClass;
    this.methodTable = methodTable;
    this.funPtrTable = new FunPtrTable(this);

    this.recordTypeTable = new RecordTypeTable(units, this, providedRecordTypes);

    for(GimpleCompilationUnit unit : units) {
      functions.addAll(unit.getFunctions());
      for(GimpleVarDecl varDecl : unit.getGlobalVariables()) {
        translateGlobalVarDecl(varDecl);
      }
    }
    
    builtinCallTranslators.add(new MallocCallTranslator("malloc"));
    builtinCallTranslators.add(FunPtrCallTranslator.INSTANCE);
    builtinCallTranslators.add(StaticCallTranslator.INSTANCE);

  }

  public RecordTypeTable getRecordTypeTable() {
    return recordTypeTable;
  }

  private void translateGlobalVarDecl(GimpleVarDecl varDecl) {
   
    try {
      
      ImType type = resolveType(varDecl.getType());
  
      type.defineField(mainClass, varDecl.getName(), false);

      globalVariables.put(varDecl.getName(), type.createFieldExpr(
          null, new SyntheticJimpleType(mainClass.getFqcn()),
          varDecl.getName()));
      
    } catch(Exception e) {
      throw new RuntimeException("Exception translating global variable '" + varDecl.getName() + "'", e);
    }
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
    return new GccFunction(mainClass.getFqcn(), function.getMangledName(), returnType, paramTypes);
  }

  public ImExpr findGlobal(String name) {
    if(globalVariables.containsKey(name)) {
      return globalVariables.get(name);
    }
    Field field = methodTable.findGlobal(name);
    if(field != null) {
      return new PrimitiveFieldExpr(field);
    }
    return null;
  }

  public ImType resolveType(GimpleType type) {
    if (type instanceof GimplePrimitiveType) {
      return ImPrimitiveType.valueOf(type);

    } else if (type instanceof GimpleRecordType) {
      return resolveRecordType((GimpleRecordType) type);

    } else if(type instanceof GimpleFunctionType) {
      return funPtrTable.resolveFunctionType((GimpleFunctionType) type);

    } else if (type instanceof GimpleIndirectType) {
      return resolveType(type.getBaseType()).pointerType();

    } else if (type instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) type;
      return resolveType(arrayType.getComponentType()).arrayType(
          arrayType.getLbound(), arrayType.getUbound());

    } else if (type instanceof GimpleVoidType) {
      return ImVoidType.INSTANCE;
    }
    throw new UnsupportedOperationException(type.toString());
  }

  public JimpleOutput getJimpleOutput() {
    return mainClass.getOutput();
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

  public CallTranslator getCallTranslator(GimpleCall call) {
    for(CallTranslator translator : Iterables.concat(methodTable.getCallTranslators(), builtinCallTranslators)) {
      if(translator.accept(call)) {
        return translator;
      }
    }
    throw new UnsupportedOperationException("No matching call translator");
  }

  
}
