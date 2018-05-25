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
package org.renjin.s4;

import org.renjin.eval.Context;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.*;

import java.util.*;

/**
 * A generic is a function that can have many {@code Methods} that handle different classes of arguments.
 *
 * <p>For example, one generic is {@code dim}, which has many methods defined in the Matrix package
 * for different classes of matrices.</p>
 */
public class Generic {

  private static final Set<String> ARITH_GROUP = Sets.newHashSet("+", "-", "*", "^", "%%", "%/%", "/");

  private static final Set<String> COMPARE_GROUP = Sets.newHashSet("==", ">", "<", "!=", "<=", ">=");

  private static final Set<String> LOGIC_GROUP = Sets.newHashSet("!", "&", "&&", "|", "||", "xor");

  private final String name;
  private final String packageName;
  private final List<String> group;
  private final String subGroup;
  private final boolean stdGeneric;
  private final Closure genericFunction;

  public static Generic primitive(String name, List<String> group) {
    return new Generic(name, group, "base", null);
  }

  public static Generic standardGeneric(Context context, String fname, String packageName) {

    Closure genericFunction = findGenericFunction(context, fname, packageName);

    List<String> group = new ArrayList<>();

    Environment namespaceEnv = getPackageNamespaceEnvironment(context, packageName);

    SEXP generic = namespaceEnv.getVariableUnsafe(fname).force(context);

    if (generic == Symbol.UNBOUND_VALUE) {
      return new Generic(fname, group, packageName, genericFunction);
    }
    if(isOps(fname)) {
      // case when selectMethod() is used to find a method for a primitive function
      group.add("Ops");
    } else {
      SEXP groupSlot = generic.getAttribute(S4.GROUP);
      if (groupSlot instanceof ListVector) {
        int groupSize = groupSlot.length();
        if (groupSize > 0) {
          for (int i = 0; i < groupSize; i++) {
            group.add(((ListVector) groupSlot).getElementAsString(i));
          }
        }
      }
    }
    return new Generic(fname, group, packageName, genericFunction);
  }

  private static Environment getPackageNamespaceEnvironment(Context context, String packageName) {
    Environment namespaceEnv;
    if (".GlobalEnv".equals(packageName)) {
      namespaceEnv = context.getGlobalEnvironment();
    } else {
      namespaceEnv = context.getNamespaceRegistry().getNamespace(context, packageName).getNamespaceEnvironment();
    }
    return namespaceEnv;
  }

  public static Closure findGenericFunction(Context context, String fname, String packageName) {
    Closure genericFunction = null;
    Frame packageFrame;
    if(".GlobalEnv".equals(packageName)) {
      packageFrame = context.getGlobalEnvironment().getFrame();
    } else {
      packageFrame = context.getNamespaceRegistry().getNamespace(context, packageName).getNamespaceEnvironment().getFrame();
    }
    SEXP foundFunction = packageFrame.getVariable(Symbol.get(fname)).force(context);
    if(foundFunction instanceof Closure) {
      SEXP funClass = foundFunction.getAttribute(Symbols.CLASS);
      if(funClass instanceof StringArrayVector) {
        StringArrayVector fclass = (StringArrayVector) funClass;
        if ("standardGeneric".equals(fclass.getElementAsString(0)) || "nonstandardGenericFunction".equals(fclass.getElementAsString(0))) {
          genericFunction = (Closure) foundFunction;
        }
      }
    }
    return genericFunction;
  }

  public Generic(String name, List<String> groups, String packageName, Closure genericFunction) {
    this.name = applyAliases(name);
    this.packageName = packageName;
    this.group = groups;
    this.stdGeneric = true;
    this.genericFunction = genericFunction;

    if (!group.isEmpty() && "Ops".equals(group.get(0))) {
      this.subGroup = opsSubGroupOf(name);
    } else {
      this.subGroup = null;
    }
  }

  private static String opsSubGroupOf(String name) {
    if (ARITH_GROUP.contains(name)) {
      return "Arith";
    } else if (COMPARE_GROUP.contains(name)) {
      return "Compare";
    } else if (LOGIC_GROUP.contains(name)) {
      return "Logic";
    } else {
      throw new IllegalArgumentException(name + " is not a member of the Ops group");
    }
  }

  private static String applyAliases(String name) {
    if("as.double".equals(name)) {
      return "as.numeric";
    } else {
      return name;
    }
  }

  public String getPackageName() {
    return packageName;
  }

  public String getName() {
    return name;
  }

  public boolean isGroupGeneric() {
    return group != null && group.size() > 0;
  }

  public boolean isStdGenericWithGroup() {
    return stdGeneric && group != null && group.size() > 0;
  }

  public Closure getGenericFunction() {
    return genericFunction;
  }

  public Set<String> getSignatureArgumentNames() {
    if(genericFunction != null) {
      SEXP signature = genericFunction.getAttribute(Symbol.get("signature"));
      if(signature instanceof StringArrayVector) {
        return new HashSet<>(Arrays.asList(((StringArrayVector) signature).toArray()));
      }
    }
    return new HashSet<>();
  }

  public boolean isOps() {
    return ARITH_GROUP.contains(name) || COMPARE_GROUP.contains(name) || LOGIC_GROUP.contains(name);
  }

  public static boolean isOps(String fname) {
    return ARITH_GROUP.contains(fname) || COMPARE_GROUP.contains(fname) || LOGIC_GROUP.contains(fname);
  }

  public String getSubGroup() {
    assert subGroup != null : "not a member of the Ops group";
    return subGroup;
  }

  public List<String> getGroup() {
    return group;
  }

  Symbol getGenericMethodTableName() {
    return Symbol.get(S4.METHOD_PREFIX + name + ":" + packageName);
  }

  Symbol getGroupGenericMethodTableName() {
    assert !group.isEmpty();
    return Symbol.get(S4.METHOD_PREFIX + group.get(0) + ":" + packageName);
  }

  Symbol getGroupStdGenericMethodTableName(String grp) {
    return Symbol.get(S4.METHOD_PREFIX + grp + ":" + packageName);
  }

  Symbol getSubGroupGenericMethodTableName() {
    assert subGroup != null;
    if("Compare".equals(subGroup)) {
      return Symbol.get(S4.METHOD_PREFIX + subGroup + ":methods");
    }
    return Symbol.get(S4.METHOD_PREFIX + subGroup + ":" + packageName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Generic generic = (Generic) o;
    return Objects.equals(name, generic.name) &&
        Objects.equals(subGroup, generic.subGroup) &&
        Objects.equals(group, generic.group);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, subGroup, group);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("Generic{");
    s.append(name);
    if(isOps()) {
      s.append("/").append(subGroup);
    } else if(isGroupGeneric()) {
      s.append(group);
    }
    s.append("}");
    return s.toString();
  }


  public SEXP asSEXP() {
    StringArrayVector.Builder builder = new StringVector.Builder();
    if(name.isEmpty()) {
      builder.add(group.get(0));
    } else {
      builder.add(name);
    }
    builder.setAttribute("package", StringVector.valueOf(packageName));
    return builder.build();
  }
}
