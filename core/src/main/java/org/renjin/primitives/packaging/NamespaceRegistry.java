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
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
  private Map<Symbol, Namespace> localNameMap = new HashMap<>();
  private Map<FqPackageName, Namespace> namespaceMap = Maps.newHashMap();

  private Map<Environment, Namespace> envirMap = Maps.newIdentityHashMap();

  private final Namespace baseNamespace;

  public NamespaceRegistry(PackageLoader loader, Environment baseNamespaceEnv) {
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


  /**
   *
   * @return a sequence of the "local names" of the currently loaded namespaces. Local names do not include
   * the group name of the namespace.
   */
  public Iterable<Symbol> getLoadedNamespaceNames() {
    return localNameMap.keySet();
  }

  public Iterable<Namespace> getLoadedNamespaces() {
    return namespaceMap.values();
  }
  
  public Optional<Namespace> getNamespaceIfPresent(Symbol name) {
    return Optional.ofNullable(localNameMap.get(name));
  }

  public Namespace getNamespace(Context context, String name) {
    return getNamespace(context, Symbol.get(name));
  }

  public Namespace getNamespace(Context context, Symbol symbol) {

    Namespace localMatch = localNameMap.get(symbol);
    if(localMatch != null) {
      return localMatch;
    }

    // Load package
    if(CORE_PACKAGES.contains(symbol.getPrintName())) {

      // There are a small number of "core" packages that are part of the
      // the GNU R source, and now the Renjin source. (e.g., stats, methods, datasets, etc)
      // These have the groupId "org.renjin"
      return getNamespace(context, FqPackageName.corePackage(symbol));

    } else if(FqPackageName.isQualified(symbol)) {

      // Is this namespace is explicitly qualified in the form {groupId}:{packageName}
      return getNamespace(context, FqPackageName.fromSymbol(symbol));

    } else {
      // Otherwise, we have an unqualified package name. Delegate lookup to the PackageLoader
      Optional<Package> pkg = loader.load(symbol.getPrintName());
      if(!pkg.isPresent()) {
        throw new EvalException("Could not find package '" + symbol + "'");
      }
      return load(context, pkg.get());
    }
  }

  private Namespace getNamespace(Context context, FqPackageName fqPackageName) {
    Namespace namespace = namespaceMap.get(fqPackageName);
    if(namespace != null) {
      return namespace;
    }
    Optional<Package> pkg = loader.load(fqPackageName);
    if(!pkg.isPresent()) {
      throw new EvalException("Could not load package " + fqPackageName);
    }
    return load(context, pkg.get());
  }

  private Namespace load(Context context, Package pkg) {
    try {
      // load the serialized functions/values from the classpath
      // and add them to our private namespace environment
      Namespace namespace = createNamespace(pkg);

      CharSource namespaceSource = pkg.getResource("NAMESPACE").asCharSource(Charsets.UTF_8);
      NamespaceFile namespaceFile = NamespaceFile.parseFile(context, namespaceSource);

      // set up the namespace
      namespace.populateNamespace(context);
      namespace.initExports(namespaceFile);

      context.getSession().getS4Cache().invalidate();

      // set up the imported symbols
      namespace.initImports(context, this, namespaceFile);

      // invoke the .onLoad hook
      // (Before we export symbols!)
      if(namespace.getNamespaceEnvironment().hasVariable(Symbol.get(".onLoad"))) {
        StringVector nameArgument = StringVector.valueOf(pkg.getName().getPackageName());
        context.evaluate(FunctionCall.newCall(Symbol.get(".onLoad"), nameArgument, nameArgument),
            namespace.getNamespaceEnvironment());
      }

      namespace.registerS3Methods(context, namespaceFile);
      namespace.loaded = true;

      return namespace;

    } catch(Exception e) {
      throw new EvalException("IOException while loading package " + pkg.getName() + ": " + e.getMessage(), e);
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
