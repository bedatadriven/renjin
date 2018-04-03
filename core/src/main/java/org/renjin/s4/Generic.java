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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A generic is a function that can have many {@code Methods} that handle different classes of arguments.
 *
 * <p>For example, one generic is {@code dim}, which has many methods defined in the Matrix package
 * for different classes of matrices.</p>
 */
public class Generic {

  private static final Set<String> ARITH_GROUP = Sets.newHashSet("+", "-", "*", "^", "%%", "%/%", "/");

  private static final Set<String> COMPARE_GROUP = Sets.newHashSet("==", ">", "<", "!=", "<=", ">=");

  private static final Set<String> LOGIC_GROUP = Sets.newHashSet("&", "&&", "|", "||", "xor");

  private static final String METHOD_PREFIX = ".__T__";

  private final String name;
  private final String packageName;
  private final String group;
  private final String subGroup;
  private final List<String> stdGenericGroups;
  private final boolean stdGeneric;

  public static Generic primitive(String name, String group) {
    return new Generic(name, group, "base");
  }

  public static Generic standardGeneric(Context context, String name, String packageName) {
    Environment namespaceEnv;
    if (".GlobalEnv".equals(packageName)) {
      namespaceEnv = context.getGlobalEnvironment();
    } else {
      namespaceEnv = context.getNamespaceRegistry().getNamespace(context, packageName).getNamespaceEnvironment();
    }
    SEXP generic = namespaceEnv.getVariableUnsafe(name).force(context);
    if (generic == Symbol.UNBOUND_VALUE) {
      return new Generic(name, null, packageName);
    }
    SEXP groupSlot = generic.getAttribute(Symbol.get("group"));
    if (groupSlot instanceof ListVector) {
      int groupSize = ((ListVector) groupSlot).length();
      if (groupSize > 0) {
        List<String> groups = new ArrayList<>();
        for (int i = 0; i < groupSize; i++) {
          groups.add(((ListVector) groupSlot).getElementAsString(i));
        }
        return new Generic(name, null, packageName, groups);
      }
    }
    return new Generic(name, null, packageName);
  }

  public Generic(String name, String group, String packageName, List<String> groups) {
    this.name = applyAliases(name);
    this.packageName = packageName;
    this.group = group;
    this.stdGenericGroups = groups;
    this.stdGeneric = true;

    if ("Ops".equals(group)) {
      this.subGroup = opsSubGroupOf(name);
    } else {
      this.subGroup = null;
    }
  }

  private Generic(String name, String group, String packageName) {
    this.name = applyAliases(name);
    this.packageName = packageName;
    this.group = group;
    this.stdGenericGroups = null;
    this.stdGeneric = false;

    if ("Ops".equals(group)) {
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

  public List<String> getGenericGroups() {
    return stdGenericGroups;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getName() {
    return name;
  }

  public boolean isGroupGeneric() {
    return group != null;
  }

  public boolean isStandardGeneric() {
    return stdGeneric;
  }

  public boolean isOps() {
    return "Ops".equals(group);
  }

  public String getSubGroup() {
    assert subGroup != null : "not a member of the Ops group";
    return subGroup;
  }

  public String getGroup() {
    assert group != null : "generic is not group-generic";
    return group;
  }

  public Symbol getGenericMethodTableName() {
    return Symbol.get(METHOD_PREFIX + name + ":" + packageName);
  }

  public Symbol getGroupGenericMethodTableName() {
    assert group != null;
    return Symbol.get(METHOD_PREFIX + group + ":" + packageName);
  }

  public Symbol getGroupStdGenericMethodTableName(String group) {
    assert group != null;
    return Symbol.get(METHOD_PREFIX + group + ":" + packageName);
  }

  public Symbol getSubGroupGenericMethodTableName() {
    assert subGroup != null;
    if(subGroup.equals("Compare")) {
      return Symbol.get(METHOD_PREFIX + subGroup + ":methods");
    }
    return Symbol.get(METHOD_PREFIX + subGroup + ":" + packageName);
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
      builder.add(group);
    } else {
      builder.add(name);
    }
    builder.setAttribute("package", StringVector.valueOf(packageName));
    return builder.build();
  }
}
