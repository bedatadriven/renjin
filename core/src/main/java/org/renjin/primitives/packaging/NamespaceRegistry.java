/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.SessionScoped;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.*;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.sexp.*;

import java.util.*;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Session-level registry of namespaces
 *
 */
@SessionScoped
public class NamespaceRegistry {

  private static final Symbol BASE = Symbol.get("base");

  /**
   * These packages are part of the R distribution and carry the
   * org.renjin groupId.
   */
  public static final Set<String> CORE_PACKAGES = Sets.newHashSet("datasets", "graphics", "grDevices", "hamcrest",
      "methods", "splines", "stats", "stats4", "utils", "grid", "parallel", "tools", "tcltk",
      "compiler");

  private PackageLoader loader;

  /**
   * Maps local names to namespaces
   */
  private Multimap<Symbol, Namespace> localNameMap = HashMultimap.create();
  private Map<FqPackageName, Namespace> namespaceMap = Maps.newHashMap();

  private Map<Environment, Namespace> envirMap = Maps.newIdentityHashMap();

  private final Namespace baseNamespace;

  public NamespaceRegistry(PackageLoader loader, Context context, Environment baseNamespaceEnv) {
    this.loader = loader;

    baseNamespace = new BaseNamespace(baseNamespaceEnv);
    localNameMap.put(BASE, baseNamespace);
    envirMap.put(baseNamespaceEnv, baseNamespace);
  }

  public Namespace getBaseNamespace() {
    return baseNamespace;
  }

  public Environment getBaseNamespaceEnv() {
    return getBaseNamespace().getNamespaceEnvironment();
  }

  public Namespace getNamespace(Environment envir) {
    Namespace ns = envirMap.get(envir);
    if(ns == null) {
      throw new IllegalArgumentException();
    }
    return ns;
  }


  public Iterable<Symbol> getLoadedNamespaces() {
    return localNameMap.keySet();
  }
  
  public Optional<Namespace> getNamespaceIfPresent(Symbol name) {
    Collection<Namespace> matching = localNameMap.get(name);
    if(matching.size() == 1) {
      return Optional.of(matching.iterator().next());
    } else {
      return Optional.empty();
    }
  }

  public Namespace getNamespace(Context context, String name) {
    return getNamespace(context, Symbol.get(name));
  }

  public Namespace getNamespace(Context context, Symbol symbol) {
    if(symbol.getPrintName().equals("base")) {
      return baseNamespace;
    }

    // try to match name to currently loaded namespaces
    for (FqPackageName fqPackageName : namespaceMap.keySet()) {
      if(symbol.getPrintName().equals(fqPackageName.toString('.')) ||
          symbol.getPrintName().equals(fqPackageName.getPackageName())) {
        return namespaceMap.get(fqPackageName);
      }
    }

    // There are a small number of "core" packages that are part of the 
    // the GNU R source, and now the Renjin source. (e.g., stats, methods, datasets, etc)
    // These have the groupId "org.renjin"
    
    if(CORE_PACKAGES.contains(symbol.getPrintName())) {
      return getNamespace(context, FqPackageName.corePackage(symbol));
    }

    // Otherwise, try to guess the groupId
    List<FqPackageName> candidates = Lists.newArrayList();

    if(couldBeFullyQualified(symbol)) {
      candidates.add(FqPackageName.fromSymbol(symbol));
    }

    // Try bioconductor first as some packages have moved from cran to bioconductor
    candidates.add(new FqPackageName("org.renjin.bioconductor", symbol.getPrintName()));
    candidates.add(new FqPackageName("org.renjin.cran", symbol.getPrintName()));

    Optional<Namespace> namespace = empty();

    for (FqPackageName candidate : candidates) {
      namespace = tryGetNamespace(context, candidate);
      if(namespace.isPresent()) {
        break;
      }
    }

    if(!namespace.isPresent()) {
      throw new EvalException("Could not load package " + symbol + "; tried " +
          Joiner.on(", ").join(candidates));
    }

    return namespace.get();
  }

  public static Set<String>  getCorePackages() {
    return CORE_PACKAGES;
  }

  public Namespace getNamespace(Context context, FqPackageName fqPackageName) {
    Optional<Namespace> namespace = tryGetNamespace(context, fqPackageName);
    if(!namespace.isPresent()) {
      throw new EvalException("Could not load package " + fqPackageName);
    }
    return namespace.get();
  }

  /**
   * Tries to obtain a reference to a namespace using it's fully qualified name,
   * either from among those loaded or those available through the package loader.
   *
   * @param context
   * @param fqName the fully-qualified package name
   * @return the corresponding {@code Namespace}, or {@code null}
   */
  private Optional<Namespace> tryGetNamespace(Context context, FqPackageName fqName) {
    if(namespaceMap.containsKey(fqName)) {
      return of(namespaceMap.get(fqName));
    } else {
      return tryLoad(context, fqName);
    }
  }

  private Optional<Namespace> tryLoad(Context context, FqPackageName fqName) {

    Optional<Package> loadResult = loader.load(fqName);
    if(!loadResult.isPresent()) {
      return empty();
    } else {
      Package pkg = loadResult.get();
      try {
        // load the serialized functions/values from the classpath
        // and add them to our private namespace environment
        Namespace namespace = createNamespace(pkg);

        CharSource namespaceSource = pkg.getResource("NAMESPACE").asCharSource(Charsets.UTF_8);
        NamespaceFile namespaceFile = NamespaceFile.parse(context, namespaceSource);

        // set up the namespace
        namespace.populateNamespace(context);
        namespace.initExports(namespaceFile);

        // set up the imported symbols
        namespace.initImports(context, this, namespaceFile);

        namespace.initS4(context);

        // invoke the .onLoad hook
        // (Before we export symbols!)
        if(namespace.getNamespaceEnvironment().hasVariable(Symbol.get(".onLoad"))) {
          StringVector nameArgument = StringVector.valueOf(pkg.getName().getPackageName());
          context.evaluate(FunctionCall.newCall(Symbol.get(".onLoad"), nameArgument, nameArgument),
              namespace.getNamespaceEnvironment());
        }

        namespace.registerS3Methods(context, namespaceFile);

        // S4 classes are declared in a namespace, but once the namespace is loaded,
        // they are loaded into the global metadata cache
        maybeUpdateS4MetadataCache(context, namespace);

        namespace.loaded = true;

        return of(namespace);

      } catch(Exception e) {
        throw new EvalException("IOException while loading package " + fqName + ": " + e.getMessage(), e);
      }
    }
  }

  private boolean couldBeFullyQualified(Symbol name) {
    String string = name.getPrintName();
    return string.indexOf(':') != -1 || string.indexOf('.') != -1;
  }


  private static void maybeUpdateS4MetadataCache(Context context, Namespace namespace) {

    if(namespace.getFullyQualifiedName().getGroupId().equals("org.renjin") &&
        namespace.getFullyQualifiedName().getPackageName().equals("methods")) {
      return;
    }

    //methods:::cacheMetaData(ns, TRUE, ns)
    Optional<Namespace> methods = context.getNamespaceRegistry()
        .getNamespaceIfPresent(Symbol.get("methods"));
    if(methods.isPresent() && methods.get().isLoaded()) {
      SEXP cacheFunction = methods.get().getEntry(Symbol.get("cacheMetaData"));
      FunctionCall cacheCall = FunctionCall.newCall(cacheFunction,
          namespace.getNamespaceEnvironment(),
          LogicalVector.TRUE,
          namespace.getNamespaceEnvironment());

      context.evaluate(cacheCall);
    }
  }

  public boolean isRegistered(Symbol name) {
    return localNameMap.containsKey(name);
  }

  public Namespace getBase() {
    return baseNamespace;
  }

  /**
   * Creates a new empty namespace
   */
  public Namespace createNamespace(Package pkg) {
    // each namespace has environment which is the leaf in a hierarchy that
    // looks like this:
    // BASE-NS -> IMPORTS -> ENVIRONMENT

    Environment imports = Environment.createNamedEnvironment(getBaseNamespaceEnv(),
        "imports:" + pkg.getName().toString('.')).build();

    Environment namespaceEnv = Environment.createNamespaceEnvironment(imports, pkg.getName().getPackageName()).build();
    Namespace namespace = new Namespace(pkg, namespaceEnv);
    localNameMap.put(pkg.getName().getPackageSymbol(), namespace);
    namespaceMap.put(pkg.getName(), namespace);

    envirMap.put(namespaceEnv, namespace);

    // save the name to the environment
    namespaceEnv.setVariableUnsafe(".packageName", StringVector.valueOf(pkg.getName().getPackageName()));
    return namespace;
  }

  public boolean isNamespaceEnv(Environment envir) {
    return envirMap.containsKey(envir);
  }

}
