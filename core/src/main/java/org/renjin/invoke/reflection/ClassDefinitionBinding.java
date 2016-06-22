package org.renjin.invoke.reflection;


import org.renjin.eval.EvalException;
import org.renjin.invoke.ClassBinding;
import org.renjin.sexp.Symbol;

/**
 * Binding for an instance of type {@code java.lang.Class}
 * 
 * <p>Provides bindings <strong>both</strong> for static methods of the class described by 
 * {@code classInstance}, as well as the methods and properties of the actual {@code java.lang.Class} object
 * such as {@link Class#getName()} or {@link Class#hashCode()}</p>
 */
public class ClassDefinitionBinding implements ClassBinding {

  private static final Symbol NEW = Symbol.get("new");

  private Class classInstance;
  private final ClassBindingImpl classBinding;
  private final ClassBindingImpl javaLangClassBinding;

  public ClassDefinitionBinding(Class classInstance, ClassBindingImpl classBinding) {
    this.classInstance = classInstance;
    this.classBinding = classBinding;
    this.javaLangClassBinding = ClassBindingImpl.get(Class.class);
  }

  @Override
  public MemberBinding getMemberBinding(Symbol name) {
    if(name == NEW) {
      return classBinding.getConstructorBinding();
    }

    // First check to see if this symbol matches a static binding 
    // for the class being described, for example java.util.HashMap
    final MemberBinding staticMember = classBinding.getStaticMember(name);
    if(staticMember != null) {
      return staticMember;
    }
    
    final MemberBinding instanceMember = javaLangClassBinding.getMemberBinding(name);
    if(instanceMember != null) {
      return instanceMember;
    }
      
    throw new EvalException("Class %s has no static member named '%s', nor does java.lang.Class have an " +
        "instance member named '%s'",
          classBinding.getBoundClass().getName(), name, name);

  }
}
