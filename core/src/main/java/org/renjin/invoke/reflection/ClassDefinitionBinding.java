package org.renjin.invoke.reflection;


import org.renjin.eval.EvalException;
import org.renjin.invoke.ClassBinding;
import org.renjin.sexp.Symbol;

public class ClassDefinitionBinding implements ClassBinding {

  private static final Symbol NEW = Symbol.get("new");

  private final ClassBindingImpl classBinding;

  public ClassDefinitionBinding(ClassBindingImpl classBinding) {
    this.classBinding = classBinding;
  }

  @Override
  public MemberBinding getMemberBinding(Symbol name) {
    if(name == NEW) {
      return classBinding.getConstructorBinding();
    }

    final MemberBinding staticMember = classBinding.getStaticMember(name);
    if(staticMember == null) {
      throw new EvalException("Class %s has no static member named '%s'",
          classBinding.getBoundClass().getName(), name);
    }

    return staticMember;
  }
}
