package org.renjin.invoke.reflection;

import com.google.common.collect.*;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.ClassBinding;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassBindingImpl implements ClassBinding {
  
  private static final IdentityHashMap<Class, ClassBindingImpl> TABLE = Maps.newIdentityHashMap();
  
  public static ClassBindingImpl get(Class clazz) {
    synchronized (TABLE) {
      ClassBindingImpl binding = TABLE.get(clazz);
      if(binding == null) {
        binding = new ClassBindingImpl(clazz);
        TABLE.put(clazz, binding);
      }
      return binding;
    }
  }
  
  private Class clazz;
  
  private ConstructorBinding constructorBinding;
  private IdentityHashMap<Symbol, MemberBinding> members = Maps.newIdentityHashMap(); 
  private IdentityHashMap<Symbol, StaticBinding> staticMembers = Maps.newIdentityHashMap();

  
  private ClassBindingImpl(Class clazz) {
    this.clazz = clazz;
            
    Map<Symbol, Method> getters = Maps.newHashMap();
    Multimap<Symbol, Method> setters = ArrayListMultimap.create();
    Multimap<Symbol, Method> methods = ArrayListMultimap.create();
    Multimap<Symbol, Method> staticMethods = ArrayListMultimap.create();

    
    for(Method method : clazz.getMethods()) {
      if((method.getModifiers() & Modifier.PUBLIC) != 0 &&
          method.getDeclaringClass() != Object.class) {
       
        if((method.getModifiers() & Modifier.STATIC) != 0 ) {
          staticMethods.put(Symbol.get(method.getName()), method);

        } else {
          String propertyName;
          if((propertyName=isGetter(method)) != null) {
            getters.put(Symbol.get(propertyName), method);
          } else if((propertyName=isSetter(method)) != null) {
            setters.put(Symbol.get(propertyName), method);
          } else {
            methods.put(Symbol.get(method.getName()), method);
          }
        }
      }
    }
    
    // any setters without matching getters will be treated as methods
    for(Symbol name : Lists.newArrayList(setters.keySet())) {
      if(!getters.containsKey(name)) {
        for(Method setter : setters.removeAll(name)) {
          methods.put(Symbol.get(setter.getName()), setter);
        }
      }
    }
    
    for(Symbol name : Sets.union(methods.keySet(), Sets.union(getters.keySet(), setters.keySet()))) {
      if(methods.containsKey(name)) {
        members.put(name, new MethodBinding(name, methods.get(name)));
      
        // TODO: add hidden getters / setters as methods
      } else {
        members.put(name, new PropertyBinding(name, getters.get(name), setters.get(name)));
      }
    }
    
    for(Symbol name : staticMethods.keySet()) {
      this.staticMembers.put(name, new StaticBinding(new MethodBinding(name, staticMethods.get(name))));
    }

    for(Field field : clazz.getFields()) {
      if(Modifier.isPublic(field.getModifiers()) &&
         Modifier.isStatic(field.getModifiers())) {
        Symbol name = Symbol.get(field.getName());
        staticMembers.put(name, new StaticBinding(new FieldBinding(field)));
      }
    }
    
    this.constructorBinding = new ConstructorBinding(clazz.getConstructors());
  }

  private String isGetter(Method method) {
    // check signature
    if(method.getParameterTypes().length != 0) {
      return null;
    }
    
    String name = method.getName();
    if(name.startsWith("get") && 
       name.length() > "get".length()) {
      
      return name.substring(3,4).toLowerCase() + name.substring(4);
    }
    
    if(name.startsWith("is") &&
       name.length() > "is".length()) {
      
      return name.substring(2,3).toLowerCase() + name.substring(3);
    }
    
    return null;
  }
  
  private String isSetter(Method method) {
    if(method.getParameterTypes().length != 1) {
      return null;
    }
    
    String name = method.getName();
    if(name.startsWith("set") &&
        name.length() > "set".length()) {
      return name.substring(3,4).toLowerCase() + name.substring(4);
    }
    
    return null;
  }

  public Set<Symbol> getMembers() {
    return members.keySet();
  }

  @Override
  public MemberBinding getMemberBinding(Symbol name) {
    MemberBinding memberBinding = members.get(name);
    if(memberBinding == null) {
      throw new EvalException("Instance of class %s has no member named '%s'",
          getBoundClass().getName(), name.getPrintName());
    }
    return memberBinding;
  }
  
  public Set<Symbol> getStaticMembers() {
    return staticMembers.keySet();
  }
  
  public StaticBinding getStaticMember(Symbol name) {
    return staticMembers.get(name);
  }
  
  public StaticBinding getStaticMember(String name) {
    return getStaticMember(Symbol.get(name));
  }

  public Object newInstance(Context context, List<SEXP> constructorArgs) {
    return constructorBinding.newInstance(context, constructorArgs);
  }

  public Class getBoundClass() {
    return clazz;
  }

  public ConstructorBinding getConstructorBinding() {
    return constructorBinding;
  }
}
