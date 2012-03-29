package org.renjin.jvminterop;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ClassBinding {
  
  private static final IdentityHashMap<Class, ClassBinding> TABLE = Maps.newIdentityHashMap();
  
  public static ClassBinding get(Class clazz) {
    synchronized (TABLE) {
      ClassBinding binding = TABLE.get(clazz);
      if(binding == null) {
        binding = new ClassBinding(clazz);
        TABLE.put(clazz, binding);
      }
      return binding;
    }
  }
  
  private Class clazz;
  
  private ConstructorBinding constructorBinding;
  private IdentityHashMap<Symbol, MemberBinding> members = Maps.newIdentityHashMap(); 
  private IdentityHashMap<Symbol, SEXP> staticMembers = Maps.newIdentityHashMap();
  
  private ClassBinding(Class clazz) {
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
      this.staticMembers.put(name, new MethodFunction(null, 
          new FunctionBinding(staticMethods.get(name))));
    }
    
    this.constructorBinding = new ConstructorBinding(clazz.getConstructors());
    if(!this.constructorBinding.isEmpty()) {
      this.staticMembers.put(Symbol.get("new"), new ConstructorFunction(constructorBinding));
    }
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

  public MemberBinding getMemberBinding(Symbol name) {
    return members.get(name);
  }
  
  public Set<Symbol> getStaticMembers() {
    return staticMembers.keySet();
  }
  
  public SEXP getStaticMember(Symbol name) {
    return staticMembers.get(name);
  }
  
  public SEXP getStaticMember(String name) {
    return getStaticMember(Symbol.get(name));
  }

  public Object newInstance(Context context, List<SEXP> constructorArgs) {
    return constructorBinding.newInstance(context, constructorArgs);
  }

  public Class getBoundClass() {
    return clazz;
  }
}
