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

import org.renjin.repackaged.guava.collect.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

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

  private final String name;
  private final String packageName;
  private final List<String> groups;

  public Generic(String name, String group) {
    this.name = applyAliases(name);
    this.packageName = "base";

    if (group == null) {
      this.groups = emptyList();
    } else if (group.equals("Ops")) {
      this.groups = Arrays.asList(opsSubGroupOf(name), "Ops");
    } else {
      this.groups = singletonList(group);
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

  public List<String> getGroups() {
    return groups;
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
        Objects.equals(groups, generic.groups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, groups);
  }

  @Override
  public String toString() {
    return "Generic{" +
        "name='" + name + '\'' +
        ", packageName='" + packageName + '\'' +
        ", groups=" + groups +
        '}';
  }
}
