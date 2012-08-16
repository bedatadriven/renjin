package org.renjin.gcc.translate;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.type.FunctionPointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.jimple.*;

import java.util.List;
import java.util.Set;


public class FunPtrTable {

  public static final String PACKAGE_NAME = "org.renjin.gcc.runtime";

  private Set<FunSignature> interfaces = Sets.newHashSet();
  private Set<JimpleMethodRef> invokers = Sets.newHashSet();

  private TranslationContext context;

  public FunPtrTable(TranslationContext context) {
    this.context = context;
  }

  public FunSignature signature(FunctionPointerType type) {
    JimpleType returnType = context.resolveType(type.getReturnType()).returnType();
    List<JimpleType> paramTypes = Lists.newArrayList();
    for(GimpleType paramType : type.getArguments()) {
      paramTypes.add(context.resolveType(paramType).paramType());
    }
    return new FunSignature(returnType, paramTypes);
  }

  private String getInterfaceName(FunSignature signature) {
    if(!interfaces.contains(signature)) {
      addInterface(signature);
    }
    return PACKAGE_NAME + "." + signature.interfaceName();
  }

  public String getInterfaceName(JimpleMethodRef ref) {
    return getInterfaceName(new FunSignature(ref));
  }

  public String getInterfaceName(FunctionPointerType type) {
    return getInterfaceName(signature(type));
  }

  public JimpleMethodRef methodRef(FunctionPointerType type) {
    FunSignature signature = signature(type);
    String interfaceName = getInterfaceName(type);
    return new JimpleMethodRef(interfaceName, "apply", signature.getReturnType(), signature.getParameterTypes());
  }

  private void addInterface(FunSignature signature) {
    JimpleInterfaceBuilder iface = context.getJimpleOutput().newInterface();
    iface.setPackageName(PACKAGE_NAME);
    iface.setClassName(signature.interfaceName());

    JimpleMethodBuilder applyMethod = iface.newMethod();
    applyMethod.setName("apply");
    applyMethod.setReturnType(signature.getReturnType());

    int paramIndex = 0;
    for(JimpleType paramType : signature.getParameterTypes()) {
      applyMethod.addParameter(paramType, "p" + paramIndex);
      paramIndex ++;
    }

    interfaces.add(signature);
  }

  public String getInvokerClassName(JimpleMethodRef method) {
    String invokerName = invokerName(method);

    if(!invokers.contains(method)) {

      JimpleClassBuilder invokerClass = context.getJimpleOutput().newClass();
      invokerClass.setClassName(invokerName);
      invokerClass.addInterface(getInterfaceName(method));

      JimpleMethodBuilder applyMethod = invokerClass.newMethod();
      applyMethod.setModifiers(JimpleModifiers.PUBLIC);
      applyMethod.setName("apply");
      applyMethod.setReturnType(method.getReturnType());

      int paramIndex = 0;
      for(JimpleType type : method.getParameterTypes()) {
        applyMethod.addParameter(type, "p" + paramIndex);
        paramIndex++;
      }

      StringBuilder call = new StringBuilder();
      call.append("staticinvoke ")
          .append(method.signature())
          .append("(");
      for(paramIndex=0;paramIndex!=method.getParameterTypes().size();++paramIndex) {
        if(paramIndex > 0) {
          call.append(", ");
        }
        call.append("p").append(paramIndex);
      }
      call.append(")");

      if(method.getReturnType().toString().equals("void")) {
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

  private String invokerName(JimpleMethodRef method) {
    return method.getClassName() + "$" + method.getMethodName() + "$" + new FunSignature(method).interfaceName();
  }
}
