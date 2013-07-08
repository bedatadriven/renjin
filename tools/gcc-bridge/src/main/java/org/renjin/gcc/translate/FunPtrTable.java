package org.renjin.gcc.translate;

import java.util.List;
import java.util.Set;

import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleInterfaceBuilder;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleModifiers;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.call.MethodRef;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.gcc.translate.type.ImFunctionType;

public class FunPtrTable {

  public static final String PACKAGE_NAME = "org.renjin.gcc.runtime";

  private Set<ImFunctionType> interfaces = Sets.newHashSet();
  private Set<MethodRef> invokers = Sets.newHashSet();

  private TranslationContext context;

  public FunPtrTable(TranslationContext context) {
    this.context = context;
  }

  public ImFunctionType resolveFunctionType(GimpleFunctionType type) {
    JimpleType returnType = context.resolveType(type.getReturnType()).returnType();
    List<JimpleType> paramTypes = Lists.newArrayList();
    for (GimpleType paramType : type.getArgumentTypes()) {
      paramTypes.add(context.resolveType(paramType).paramType());
    }
    return new ImFunctionType(returnType, paramTypes);
  }

  private String getInterfaceName(ImFunctionType signature) {
    if (!interfaces.contains(signature)) {
      addInterface(signature);
    }
    return PACKAGE_NAME + "." + signature.interfaceName();
  }

  public String getInterfaceName(MethodRef ref) {
    return getInterfaceName(new ImFunctionType(ref));
  }

  public String getInterfaceName(GimpleFunctionType type) {
    return getInterfaceName(resolveFunctionType(type));
  }

  public ImFunctionType methodRef(GimpleFunctionType type) {
    return resolveFunctionType(type);
  }

  private void addInterface(ImFunctionType signature) {
    JimpleInterfaceBuilder iface = context.getJimpleOutput().newInterface();
    iface.setPackageName(PACKAGE_NAME);
    iface.setClassName(signature.interfaceName());
    iface.extendsInterface("org.renjin.gcc.runtime.FunPtr");

    JimpleMethodBuilder applyMethod = iface.newMethod();
    applyMethod.setName("apply");
    applyMethod.setReturnType(signature.getReturnType());

    int paramIndex = 0;
    for (JimpleType paramType : signature.getParameterTypes()) {
      applyMethod.addParameter(paramType, "p" + paramIndex);
      paramIndex++;
    }

    interfaces.add(signature);
  }

  public String getInvokerClassName(MethodRef method) {
    String invokerName = invokerName(method);

    if (!invokers.contains(method)) {

      JimpleClassBuilder invokerClass = context.getJimpleOutput().newClass();
      invokerClass.setClassName(invokerName);
      invokerClass.addInterface(getInterfaceName(method));

      JimpleMethodBuilder applyMethod = invokerClass.newMethod();
      applyMethod.setModifiers(JimpleModifiers.PUBLIC);
      applyMethod.setName("apply");
      applyMethod.setReturnType(method.getReturnType());

      int paramIndex = 0;
      for (JimpleType type : method.getParameterTypes()) {
        applyMethod.addParameter(type, "p" + paramIndex);
        paramIndex++;
      }

      StringBuilder call = new StringBuilder();
      call.append("staticinvoke ").append(method.signature()).append("(");
      for (paramIndex = 0; paramIndex != method.getParameterTypes().size(); ++paramIndex) {
        if (paramIndex > 0) {
          call.append(", ");
        }
        call.append("p").append(paramIndex);
      }
      call.append(")");

      if (method.getReturnType().toString().equals("void")) {
        applyMethod.addStatement(call.toString());
        applyMethod.addStatement("return");
      } else {
        applyMethod.addVarDecl(method.getReturnType(), "_retval");
        applyMethod.addStatement("_retval = " + call.toString());
        applyMethod.addStatement("return _retval");
      }
    }
    return invokerName;
  }

  private String invokerName(MethodRef method) {
    return method.getClassName() + "$" + method.getMethodName() + "$" + new ImFunctionType(method).interfaceName();
  }
}
