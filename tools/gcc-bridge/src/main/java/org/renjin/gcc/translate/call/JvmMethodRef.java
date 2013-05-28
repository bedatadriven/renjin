package org.renjin.gcc.translate.call;

import java.lang.reflect.Method;
import java.util.List;

import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;

import com.google.common.collect.Lists;

public class JvmMethodRef extends MethodRef {

  private final Method method;

  public JvmMethodRef(Method method) {
    this.method = method;
  }

  @Override
  public JimpleType getReturnType() {
    return new RealJimpleType(method.getReturnType());
  }

  @Override
  public List<JimpleType> getParameterTypes() {
    List<JimpleType> types = Lists.newArrayList();
    for (Class type : method.getParameterTypes()) {
      types.add(new RealJimpleType(type));
    }
    return types;
  }

  @Override
  public String getDeclaringClass() {
    return method.getDeclaringClass().getName();
  }

  @Override
  public String getMethodName() {
    return method.getName();
  }

  @Override
  public String getClassName() {
    return method.getDeclaringClass().getName();
  }

}
