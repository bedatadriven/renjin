package org.renjin.primitives.packaging;


import com.google.common.collect.Sets;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.reflection.ClassBindingImpl;
import org.renjin.primitives.S3;
import org.renjin.primitives.text.regex.ExtendedRE;
import org.renjin.sexp.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

public class NamespaceInitHandler implements NamespaceDirectiveHandler {

  private Context context;
  private NamespaceRegistry registry;
  private Namespace namespace;
  private final Set<Symbol> groups = Sets.newHashSet(Symbol.get("Ops"), Symbol.get("Math"), Symbol.get("Summary"));

  public NamespaceInitHandler(Context context, NamespaceRegistry registry, Namespace namespace) {
    this.context = context;
    this.registry = registry;
    this.namespace = namespace;
  }

  @Override
  public void export(List<Symbol> symbols) {
    for(Symbol symbol : symbols) {
      namespace.addExport(symbol);
    }
  }

  @Override
  public void exportPattern(String pattern) {
    ExtendedRE re = new ExtendedRE(pattern);
    for(Symbol symbol : namespace.getNamespaceEnvironment().getSymbolNames()) {
      if(re.match(symbol.getPrintName())) {
        namespace.addExport(symbol);
      }
    }
  }

  @Override
  public void import_(List<String> packageNames) {
    for(String packageName : packageNames) {
      Namespace importedNamespace = registry.getNamespace(packageName);
      importedNamespace.copyExportsTo(namespace.getImportsEnvironment());
    }
  }

  @Override
  public void S3method(Symbol genericName, String className) {
    String methodName = genericName.getPrintName() + "." + className;
    S3method(genericName, className, Symbol.get(methodName));
  }

  @Override
  public void importFrom(String packageName, List<Symbol> symbols) {
    Namespace importedNamespace = registry.getNamespace(packageName);
    for(Symbol symbol : symbols) {
      SEXP imported = importedNamespace.getExport(symbol);
      namespace.getNamespaceEnvironment().setVariable(symbol, imported);
    }
  }

  @Override
  public void importFromClass(String className, List<Symbol> methods) {
    Class clazz = namespace.getPackage().loadClass(className);
    ClassBindingImpl classBinding = ClassBindingImpl.get(clazz);

    for(Symbol method : methods) {
      namespace.getNamespaceEnvironment().setVariable(method, classBinding.getStaticMember(method).getValue());
    }
  }

  @Override
  public void importClass(List<String> classNames) {
    for(String className : classNames) {
      try {
        Class clazz = namespace.getPackage().loadClass(className);
        namespace.getNamespaceEnvironment().setVariable(clazz.getSimpleName(), new ExternalPtr(clazz));
      } catch(Exception e) {
        throw new EvalException("Could not load class " + className);
      }   
    }
  }

  @Override
  public void S3method(Symbol genericName, String className, Symbol methodName) {

    Function method = resolveS3Method(methodName);

    Environment definitionEnv = resolveS3DefinitionEnvironment(genericName);
    if(definitionEnv == null || definitionEnv == Environment.EMPTY) {
      throw new EvalException("Could not find definition environment for " + genericName);
    }
    if (!definitionEnv.hasVariable(S3.METHODS_TABLE)) {
      definitionEnv.setVariable(S3.METHODS_TABLE, Environment.createChildEnvironment(context.getBaseEnvironment()));
    }
    Environment methodsTable = (Environment) definitionEnv.getVariable(S3.METHODS_TABLE);
    methodsTable.setVariable(methodName, method);
  }

  private Environment resolveS3DefinitionEnvironment(Symbol genericName) {
    Environment definitionEnv;
    if (groups.contains(genericName)) {
      definitionEnv = registry.getBaseNamespaceEnv();
    } else {
      SEXP genericFunction = namespace.getNamespaceEnvironment().findFunction(context, genericName);
      if (genericFunction == null) {
        System.err.println("Cannot find S3 method definition '" + genericName + "'");
        for (Symbol sym : namespace.getNamespaceEnvironment().getParent().getSymbolNames()) {
          System.err.println("imported: " + sym);
        }
        throw new EvalException("Cannot find S3 method definition '" + genericName + "'");
      }
      definitionEnv = getDefinitionEnv(genericFunction);
    }
    return definitionEnv;
  }

  private Function resolveS3Method(Symbol methodName) {
    SEXP methodExp = namespace.getNamespaceEnvironment().getVariable(methodName).force(context);
    if (methodExp == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Missing export: " + methodName + " not found in " + namespace.getName());
    }
    if (!(methodExp instanceof Function)) {
      throw new IllegalStateException(methodName + ": expected function but was " + methodExp.getClass().getName());
    }
    return (Function) methodExp;
  }

  private Environment getDefinitionEnv(SEXP genericFunction) {
    if(genericFunction instanceof Closure) {
      return ((Closure) genericFunction).getEnclosingEnvironment();
    } else if(genericFunction instanceof PrimitiveFunction) {
      return registry.getBaseNamespaceEnv();
    } else {
      throw new IllegalArgumentException(genericFunction.getClass().getName());
    }
  }

  @Override
  public void useDynlib(String libraryName, List<DynlibEntry> entries, boolean register, String fixes) {
    try {
      FqPackageName packageName = namespace.getPackage().getName();
      String className = packageName.getGroupId() + "." + packageName.getPackageName() + "." + libraryName;
      Class clazz = namespace.getPackage().loadClass(className);

      if(entries.isEmpty()) {
        // add all methods from class file
        for(Method method : clazz.getMethods()) {
          if(isPublicStatic(method)) {
            addGnurMethod(fixes + method.getName(), method);
          }
        }
      } else {
        for(DynlibEntry entry : entries) {
          Method method = findGnurMethod(clazz, entry.getSymbolName());
          if(entry.getAlias() != null) {
            addGnurMethod(fixes + entry.getAlias(), method);
          } else {
            addGnurMethod(fixes + entry.getSymbolName(), method);
          }
        }
      }
    } catch(Exception e) {
      // Allow the program to continue, there may be some packages whose gnur
      // compilation failed but can still partially function.
      System.err.println("WARNING: Failed to import dynLib entries for " + namespace.getName() + ", expect subsequent failures");
    }
  }

  private void addGnurMethod(String name, Method method) {
    namespace.getImportsEnvironment().setVariable(name,
        new ExternalPtr<Method>(method));
  }

  private boolean isPublicStatic(Method method) {
    return Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers());
  }

  private Method findGnurMethod(Class clazz, String symbolName) {
    for(Method method : clazz.getMethods()) {
      if(method.getName().equals(symbolName) && isPublicStatic(method)) {
        return method;
      }
    }
    throw new RuntimeException("Couldn't find method '" + symbolName + "'");
  }
}
